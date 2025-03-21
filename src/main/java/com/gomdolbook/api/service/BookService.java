package com.gomdolbook.api.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.BookSearchResponseDTO;
import com.gomdolbook.api.api.dto.ReadingLogUpdateRequestDTO;
import com.gomdolbook.api.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.models.BookModel;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.service.Auth.SecurityService;
import com.gomdolbook.api.service.Auth.UserService;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class BookService {

    private final BookRepository bookRepository;
    private final UserService userService;
    private final WebClient webClient;
    private final ReadingLogRepository readingLogRepository;
    private final SecurityService securityService;
    private final AsyncCache<String, AladinAPI> aladinAPIAsyncCache;

    @Value("${api.aladin.ttbkey}")
    private String ttbkey;

    @Cacheable(cacheNames = "readingLogCache", keyGenerator = "customKeyGenerator", unless = "#result == null")
    @UserCheckAndSave
    @Transactional(readOnly = true)
    public BookAndReadingLogDTO getReadingLog(String isbn) {
        return bookRepository.findByUserEmailAndIsbn(
                securityService.getUserEmailFromSecurityContext(), isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }  

    @Cacheable(cacheNames = "statusCache", keyGenerator = "customKeyGenerator", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getStatus(String isbn) {
        Optional<Status> status = bookRepository.getStatus(isbn, securityService.getUserEmailFromSecurityContext());
        return status.map(Enum::name).orElse("EMPTY");
    }

    @Cacheable(cacheNames = "bookByIsbnCache", key = "#isbn", unless = "#result == null or #result.isEmpty()")
    @Transactional(readOnly = true)
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn13(isbn);
    }

    public Mono<BookDTO> fetchItemFromAladin(String isbn13) {
        return getAladinData(isbn13,"ItemLookUp.aspx", uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("ItemIdType", "ISBN13")
            .queryParam("ItemId", isbn13)
            .queryParam("Cover", "Big")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(BookModel::toBookDTO);
    }

    public Mono<List<BookSearchResponseDTO>> searchBookFromAladin(String query) {
        return getAladinData(query, "ItemSearch.aspx", uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("Query", query)
            .queryParam("Cover", "MidBig")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(BookModel::toListBookSearchResponseDTO);
    }

    private Mono<AladinAPI> getAladinData(String key,String uri, Function<UriBuilder, URI> uriBuilder) {
        String cacheKey = uri + key;
        return Mono.fromCompletionStage(aladinAPIAsyncCache.get(cacheKey,
                (s, executor) -> fetchAladinData(uri, uriBuilder).exceptionally(ex -> {
                    log.info("API 요청 실패, uri: {}, key: {}", uri, key);
                    return null;
                })));
    }

    private CompletableFuture<AladinAPI> fetchAladinData(String uri,
        Function<UriBuilder, URI> uriBuilder) {
        return executeFetchAladinRequest(uri, uriBuilder).toFuture();
    }

    private Mono<AladinAPI> executeFetchAladinRequest(String uri, Function<UriBuilder, URI> uriBuilder) {
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
            .bodyToMono(AladinAPI.class)
            .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(2))
                .doBeforeRetry(
                    retrySignal -> log.info("[retry] {}", retrySignal.toString())));
    }

    @CacheEvict(cacheNames = "statusCache", key = "@securityService.getCacheKey(#requestDTO.isbn13())")
    @UserCheckAndSave
    @Transactional
    public Book saveOrUpdateBook(BookSaveRequestDTO requestDTO) {
        User user = userService.findByEmail(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));

        Optional<ReadingLog> readingLogOptional = readingLogRepository.findByIsbnAndEmail(
            requestDTO.isbn13(),
            securityService.getUserEmailFromSecurityContext());

        if (readingLogOptional.isPresent()) {
            ReadingLog readingLog = readingLogOptional.get();
            readingLog.updateStatus(validateAndConvertStatus(requestDTO.status()));

            return readingLog.getBook();
        }
        Book book = Book.builder()
            .title(requestDTO.title())
            .author(requestDTO.author())
            .pubDate(requestDTO.pubDate())
            .description(requestDTO.description())
            .isbn13(requestDTO.isbn13())
            .cover(requestDTO.cover())
            .categoryName(requestDTO.categoryName())
            .publisher(requestDTO.publisher())
            .build();

        String status = (requestDTO.status() == null || requestDTO.status().isBlank()) ? "NEW"
            : requestDTO.status();

        ReadingLog readingLog = new ReadingLog(validateAndConvertStatus(status), "",
            "", "", 0);

        readingLog.setUser(user);

        ReadingLog savedReadingLog = readingLogRepository.save(readingLog);
        book.setReadingLog(savedReadingLog);

        return bookRepository.save(book);

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
    public List<BookListResponseDTO> getLibrary(String status) {
        return bookRepository.findByReadingStatus(validateAndConvertStatus(status),
            securityService.getUserEmailFromSecurityContext());
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#request.isbn())")
    @Transactional
    public void updateReadingLog(ReadingLogUpdateRequestDTO request) {
        Book book = bookRepository.findByIsbn13(request.isbn())
            .orElseThrow(() -> new BookNotFoundException("Cannot find book: " + request.isbn()));

        ReadingLog readingLog = book.getReadingLog();

        switch (request.note()) {
            case "note1"-> readingLog.updateNote1(request.value());
            case "note2" -> readingLog.updateNote2(request.value());
            case "note3"-> readingLog.updateNote3(request.value());
            default -> throw new IllegalArgumentException("Invalid note: " + request.note());
        }
    }

    @CacheEvict(cacheNames = "statusCache", key = "@securityService.getCacheKey(#isbn)")
    @Transactional
    public void updateStatus(String isbn, String status) {
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail(isbn,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException("can't not find book: " + isbn));

        readingLog.updateStatus(validateAndConvertStatus(status));
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#isbn)")
    @Transactional
    public void saveOrUpdateRating(int rating, String isbn) {
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail(isbn,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException("can't find book: " + isbn));

        readingLog.updateRating(rating);
    }
}
