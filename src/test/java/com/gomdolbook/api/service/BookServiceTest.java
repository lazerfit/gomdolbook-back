package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.ReadingLogDTO;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
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
@Slf4j
class BookServiceTest {

    static MockWebServer server;

    @Autowired
    BookService bookService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @BeforeAll
    static void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @BeforeEach
    void setup() {
        Book book = Book.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image1")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();
        bookRepository.save(book);
    }

    @AfterEach
    void teardown() {
        bookRepository.deleteAll();
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
        ReadingLogDTO readingLog = bookService.getReadingLog("9788991290402");
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

    @Test
    void getBookListFromAPI() throws Exception {
        String response = objectMapper.writeValueAsString(new AladinAPI(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"), new Item("소년이 온다1", "한강1", "2014-06-19", "2025 노벨문학상",
                "97889364341201", "image11", "노벨문학상1",
                "창비1"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        Mono<List<BookDTO>> list = bookService.searchBookFromAladin("글쓰기");

        StepVerifier.create(list)
            .expectNextMatches(book -> book.getLast().getTitle().equals("소년이 온다1"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void getBookError() {
        Assertions.assertThrows(BookNotFoundException.class,
            () -> bookService.getBook("11"));
    }

    @Test
    void getBook() {
        BookDTO book = bookService.getBook("9788936434120");
        assertThat(book.getTitle()).isEqualTo("소년이 온다");
    }

    @Test
    void aopInfo() {
        log.info("isAopProxy, BookService={}", AopUtils.isAopProxy(bookService));
    }

    @Test
    void getStatus() {
        String status = bookService.getStatus("9788991290402");
        assertThat(status).isEqualTo("NEW");
    }
}
