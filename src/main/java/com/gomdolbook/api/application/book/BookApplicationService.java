package com.gomdolbook.api.application.book;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.application.book.dto.AladinResponseData;
import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.SearchedBookData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.application.user.UserApplicationService;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.common.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationError;
import java.net.URI;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

@PreAuthorizeWithContainsUser
@Service
@RequiredArgsConstructor
@Slf4j
public class BookApplicationService {

    private final BookRepository bookRepository;
    private final BookMetaRepository bookMetaRepository;
    private final UserApplicationService userApplicationService;
    private final WebClient webClient;
    private final ReadingLogRepository readingLogRepository;
    private final SecurityService securityService;
    private final AsyncCache<String, AladinResponseData> aladinAPIAsyncCache;

    @Value("${api.aladin.ttbkey}")
    private String ttbkey;

    @Cacheable(cacheNames = "readingLogCache", keyGenerator = "customKeyGenerator", unless = "#result == null")
    @UserCheckAndSave
    @Transactional(readOnly = true)
    public BookAndReadingLogData getReadingLog(String isbn) {
        return bookRepository.findByEmail(
                securityService.getUserEmailFromSecurityContext(), isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }  

    @Cacheable(cacheNames = "statusCache", keyGenerator = "customKeyGenerator", unless = "#result == null")
    @Transactional(readOnly = true)
    public StatusData getStatus(String isbn) {
        Optional<Status> status = bookRepository.getStatus(isbn, securityService.getUserEmailFromSecurityContext());
        return StatusData.of(status.map(Enum::name).orElse("EMPTY"));
    }

    @Cacheable(cacheNames = "bookByIsbnCache", key = "#isbn", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Book> find(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public Mono<BookData> fetchItemFromAladin(String isbn) {
        return getAladinData(isbn,"ItemLookUp.aspx", uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("ItemIdType", "ISBN13")
            .queryParam("ItemId", isbn)
            .queryParam("Cover", "Big")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(BookData::from);
    }

    public Mono<List<SearchedBookData>> searchBookFromAladin(String query) {
        return getAladinData(query, "ItemSearch.aspx", uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("Query", query)
            .queryParam("Cover", "MidBig")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(SearchedBookData::from);
    }

    private Mono<AladinResponseData> getAladinData(String key,String uri, Function<UriBuilder, URI> uriBuilder) {
        String cacheKey = uri + key;
        return Mono.fromCompletionStage(aladinAPIAsyncCache.get(cacheKey,
                (s, executor) -> fetchAladinData(uri, uriBuilder).exceptionally(ex -> {
                    log.info("API 요청 실패, uri: {}, key: {}", uri, key);
                    return null;
                })));
    }

    private CompletableFuture<AladinResponseData> fetchAladinData(String uri,
        Function<UriBuilder, URI> uriBuilder) {
        return executeFetchAladinRequest(uri, uriBuilder).toFuture();
    }

    private Mono<AladinResponseData> executeFetchAladinRequest(String uri, Function<UriBuilder, URI> uriBuilder) {
        return webClient.get()
            .uri(uri, uriBuilder)
            .httpRequest(httpRequest -> {
                HttpClientRequest httpClientRequest = httpRequest.getNativeRequest();
                httpClientRequest.responseTimeout(Duration.ofSeconds(5));
            })
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                clientResponse -> {
                    log.error("Client error: {}", clientResponse.statusCode());
                    return Mono.error(
                        new RuntimeException("Failed to fetch book item: Client Side"));
                })
            .onStatus(HttpStatusCode::is5xxServerError,
                clientResponse -> {
                    log.error("Server error: {}", clientResponse.statusCode());
                    return Mono.error(
                        new RuntimeException("Failed to fetch book item: Server Side"));
                })
            .bodyToMono(AladinResponseData.class)
            .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(2))
                .doBeforeRetry(
                    retrySignal -> log.info("[retry] {}", retrySignal.toString())));
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "statusCache", key = "@securityService.getCacheKey(#command.isbn())"),
        @CacheEvict(cacheNames = "libraryCache", key = "@securityService.getCacheKey(#command.status())"),
        @CacheEvict(cacheNames = "finishedBookCalendarData", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @UserCheckAndSave
    @Transactional
    public Book registerBookWithMeta(BookSaveCommand command) {
        User user = userApplicationService.find(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        BookMeta bookMeta = bookMetaRepository.findByIsbn(command.isbn())
            .orElseGet(() -> bookMetaRepository.save(BookMeta.of(command)));
        Book book = createBookWithReadingLog(bookMeta, command.status(), user);
        setBookReadingPeriodByStatus(book, command.status());
        return bookRepository.save(book);
    }

    private Book createBookWithReadingLog(BookMeta bookMeta, String status,  User user) {
        Book book = Book.of(bookMeta);
        ReadingLog readingLog = setDefaultReadingLog(status, user);
        book.setReadingLog(readingLog);
        return book;
    }

    private ReadingLog setDefaultReadingLog(String initialStatus, User user) {
        String status = (initialStatus == null || initialStatus.isBlank()) ? "NEW"
            : initialStatus;
        return ReadingLog.of(user, validateAndConvertStatus(status));
    }

    private void setBookReadingPeriodByStatus(Book book, String status) {
        if (status == null) return;
        if (status.equals("READING") ) {
            ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            book.changeStartedAt(kst.toLocalDateTime());
        } else if (status.equals("FINISHED")) {
            ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            book.changeFinishedAt(kst.toLocalDateTime());
        }
    }

    private Status validateAndConvertStatus(String statusString)
        throws IllegalArgumentException {
        try {
            return Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 Status 값입니다. : " + statusString);
        }
    }

    @Cacheable(cacheNames = "libraryCache", keyGenerator = "customKeyGenerator", unless = "#result.isEmpty()")
    @Transactional
    public List<BookListData> getLibrary(String status) {
        return bookRepository.findByStatus(validateAndConvertStatus(status),
            securityService.getUserEmailFromSecurityContext());
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#command.isbn())")
    @Transactional
    public void changeReadingLog(ReadingLogUpdateCommand command) {
        Book book = bookRepository.findByIsbn(command.isbn())
            .orElseThrow(() -> new BookNotFoundException("Cannot find book: " + command.isbn()));

        ReadingLog readingLog = book.getReadingLog();

        switch (command.note()) {
            case "note1"-> readingLog.changeNote1(command.text());
            case "note2" -> readingLog.changeNote2(command.text());
            case "note3"-> readingLog.changeNote3(command.text());
            default -> throw new IllegalArgumentException("Invalid note: " + command.note());
        }
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "statusCache", key = "@securityService.getCacheKey(#isbn)"),
        @CacheEvict(cacheNames = "finishedBookCalendarData", key = "@securityService.getUserEmailFromSecurityContext()", condition = "#status.equals('FINISHED')")
    })
    @CacheEvict(cacheNames = "statusCache", key = "@securityService.getCacheKey(#isbn)")
    @Transactional
    public void changeStatus(String isbn, String status) {
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail(isbn,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException("can't not find book: " + isbn));

        readingLog.changeStatus(validateAndConvertStatus(status));
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#isbn)")
    @Transactional
    public void changeRating(int rating, String isbn) {
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail(isbn,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException("can't find book: " + isbn));

        readingLog.changeRating(rating);
    }

    @Cacheable(cacheNames = "finishedBookCalendarData", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty()")
    @Transactional
    public List<FinishedBookCalendarData> getFinishedBookCalendarData() {
        return bookRepository.getFinishedBookCalendarData(securityService.getUserEmailFromSecurityContext());
    }
}
