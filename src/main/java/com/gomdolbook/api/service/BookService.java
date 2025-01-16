package com.gomdolbook.api.service;

import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.ReadingLogDTO;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.models.BookModel;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final WebClient webClient;
    private final ReadingLogRepository readingLogRepository;

    @Value("${api.aladin.ttbkey}")
    private String ttbkey;

    @Transactional(readOnly = true)
    public ReadingLogDTO getReadingLog(String isbn13) {

        return bookRepository.findByIsbn13(isbn13).map(ReadingLogDTO::new)
            .orElseThrow(() -> new BookNotFoundException(isbn13));
    }

    @Transactional(readOnly = true)
    public String getStatus(String isbn13) {
        Optional<Book> book = bookRepository.findByIsbn13(isbn13);
        return book.map(value -> value.getReadingLog().getStatus().name()).orElse("NEW");
    }

    @Transactional(readOnly = true)
    public BookDTO getBook(String isbn13) {

        return bookRepository.findByIsbn13(isbn13).map(BookDTO::new)
            .orElseThrow(() -> new BookNotFoundException(isbn13));
    }

    public Mono<BookDTO> fetchItemFromAladin(String isbn13) {

        return executeFetchAladinRequest("ItemLookUp.aspx",uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("ItemIdType", "ISBN13")
            .queryParam("ItemId", isbn13)
            .queryParam("Cover", "MidBig")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(BookModel::convertBookDTO);
    }

    public Mono<List<BookDTO>> searchBookFromAladin(String query) {
        return executeFetchAladinRequest("ItemSearch.aspx", uriBuilder -> uriBuilder
            .queryParam("ttbkey", ttbkey)
            .queryParam("Query", query)
            .queryParam("Cover", "MidBig")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101").build())
            .map(aladinAPI -> aladinAPI.item().stream().map(BookModel::convertBookDTO).toList());
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

    @Transactional
    public void saveBook(BookSaveRequestDTO requestDTO) {
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

        ReadingLog readingLog = new ReadingLog(validateAndConvertStatus(requestDTO.status()), "",
            "", "");

        ReadingLog savedReadingLog = readingLogRepository.save(readingLog);
        book.addReadingLog(savedReadingLog);

        bookRepository.save(book);
    }

    private Status validateAndConvertStatus(String statusString)
        throws IllegalArgumentException {
        try {
            return Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 Status 값입니다. : " + statusString);
        }
    }

}
