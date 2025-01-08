package com.gomdolbook.api.service;

import com.gomdolbook.api.dto.AladinAPI;
import com.gomdolbook.api.dto.BookDTO;
import com.gomdolbook.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.dto.ReadingLogDTO;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.models.Book;
import com.gomdolbook.api.models.ReadingLog;
import com.gomdolbook.api.models.ReadingLog.Status;
import com.gomdolbook.api.repository.BookRepository;
import com.gomdolbook.api.repository.ReadingLogRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

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
    public BookDTO getBook(String isbn13) {

        return bookRepository.findByIsbn13(isbn13).map(BookDTO::new)
            .orElseThrow(() -> new BookNotFoundException(isbn13));
    }

    public Mono<BookDTO> fetchItemFromAladin(String isbn13) {

        return webClient.get()
            .uri("ItemLookUp.aspx", uriBuilder -> uriBuilder
                .queryParam("ttbkey", ttbkey)
                .queryParam("ItemIdType", "ISBN13")
                .queryParam("ItemId", isbn13)
                .queryParam("Cover", "MidBig")
                .queryParam("Output", "JS")
                .queryParam("Version", "20131101").build())
            .httpRequest(httpRequest -> {
                HttpClientRequest httpClientRequest = httpRequest.getNativeRequest();
                httpClientRequest.responseTimeout(Duration.ofSeconds(30));
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
            .map(BookDTO::new);
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
            throw new IllegalArgumentException("잘못된 Status 값입니다. : " + statusString);
        }
    }

}
