package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.dto.AladinAPI;
import com.gomdolbook.api.dto.AladinAPI.Item;
import com.gomdolbook.api.dto.BookDTO;
import com.gomdolbook.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.dto.ReadingLogDTO;
import com.gomdolbook.api.models.ReadingLog.Status;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookServiceTest {

    static MockWebServer server;

    @Autowired
    BookService bookService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.aladin.baseUrl", () -> String.format("http://localhost:%s/", server.getPort()));
        registry.add("api.aladin.ttbkey", () -> "test_key");
    }

    @Test
    void saveBookAndReadingLog() {
        BookSaveRequestDTO requestDTO = BookSaveRequestDTO.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .status("READING")
            .build();

        bookService.saveBook(requestDTO);
        BookDTO book = bookService.getBook("9788991290402");
        ReadingLogDTO readingLog = bookService.getReadingLog("9788991290402").orElseThrow();
        assertThat(book.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(readingLog.getStatus()).isEqualTo(Status.READING);
    }

    @Test
    void getBookFromAPI() throws JsonProcessingException, InterruptedException {
        String response = objectMapper.writeValueAsString(new AladinAPI(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        Mono<BookDTO> bookInfo = bookService.fetchItemFromAladin("9788936434120");

        StepVerifier.create(bookInfo)
            .expectNextMatches(book -> book.getTitle().equals("소년이 온다"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
    }
}
