package com.gomdolbook.api.service;

import com.gomdolbook.api.dto.AladinAPI;
import com.gomdolbook.api.dto.AladinAPI.Item;
import com.gomdolbook.api.dto.BookDTO;
import com.gomdolbook.api.models.Book;
import com.gomdolbook.api.repository.jpa.BookRepository;
import com.gomdolbook.api.repository.reactive.BookReactiveRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookReactiveRepository bookReactiveRepository;
    private final TransactionalOperator operator;
    private final WebClient webClient;
    @Value("${api.aladin.ttbkey}")
    private String ttbkey;

    public Mono<BookDTO> getBook(String isbn13) {

        return bookReactiveRepository.findByIsbn13(isbn13)
            .map(BookDTO::new)
            .switchIfEmpty(getItemUsingAladinAPI(isbn13))
            .as(operator::transactional);
    }

    public Mono<AladinAPI> getBookV2(String isbn13) {
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
            .bodyToMono(AladinAPI.class);
    }

    private Mono<BookDTO> getItemUsingAladinAPI(String isbn13) {

        Mono<AladinAPI> response = webClient.get()
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
                    return Mono.error(new RuntimeException("Failed to fetch book item: Client Side"));
                })
            .onStatus(HttpStatusCode::is5xxServerError,
                clientResponse -> {
                    log.error("Server error: {}", clientResponse.statusCode());
                    return Mono.error(new RuntimeException("Failed to fetch book item: Server Side"));
                })
            .bodyToMono(AladinAPI.class);


        return saveItem(response);
    }

    private Mono<BookDTO> saveItem(Mono<AladinAPI> response) {
        return response.map(value -> {

                Item item = value.item().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No Items found in API Response"));

                return Book.builder()
                    .title(item.title())
                    .author(item.author())
                    .pubDate(item.pubDate())
                    .description(item.description())
                    .isbn13(item.isbn13())
                    .cover(item.cover())
                    .categoryName(item.categoryName())
                    .publisher(item.publisher())
                    .build();
            })
            .flatMap(bookReactiveRepository::save)
            .map(BookDTO::new);
    }

}
