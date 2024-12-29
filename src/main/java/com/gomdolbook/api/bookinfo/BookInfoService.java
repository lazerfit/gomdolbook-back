package com.gomdolbook.api.bookinfo;

import com.gomdolbook.api.bookinfo.dto.AladinAPI;
import com.gomdolbook.api.bookinfo.dto.AladinAPI.Item;
import com.gomdolbook.api.bookinfo.dto.BookInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookInfoService {

    private final BookInfoRepository bookInfoRepository;
    private final BookInfoReactiveRepository bookInfoReactiveRepository;
    private final WebClient client = WebClient.create("http://www.aladin.co.kr/ttb/api/");
    private static final String TTB_KEY = System.getenv("ttbkey");

    @Transactional
    public Mono<BookInfoDTO> getBookInfo(String isbn13) {

        Mono<BookInfoDTO> aladinAPIMono = getItemUsingAladinAPI(isbn13);

        return bookInfoReactiveRepository.findByIsbn13(isbn13)
            .map(BookInfoDTO::new)
            .switchIfEmpty(aladinAPIMono);
    }

    private Mono<BookInfoDTO> getItemUsingAladinAPI(String isbn13) {
        return client.get().uri("ItemLookUp.aspx", uriBuilder -> uriBuilder
                .queryParam("ttbkey", TTB_KEY)
                .queryParam("ItemIdType", "ISBN13")
                .queryParam("ItemId", isbn13)
                .queryParam("Cover", "MidBig")
                .queryParam("Output", "JS")
                .queryParam("Version", "20131101").build())
            .retrieve()
            .onStatus(httpStatusCode -> !httpStatusCode.equals(HttpStatus.OK),
                clientResponse -> Mono.error(new RuntimeException("Failed to fetch book item")))
            .bodyToMono(AladinAPI.class)
            .map(value -> {
                Item item = value.items().stream().findFirst()
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
            .flatMap(bookInfoReactiveRepository::save)
            .map(BookInfoDTO::new);
    }
}
