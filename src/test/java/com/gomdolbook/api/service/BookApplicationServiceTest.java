package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.dto.AladinResponseData;
import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.SearchedBookData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.readingLog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.util.TestDataFactory;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
class BookApplicationServiceTest {

    static MockWebServer server;
    static User user;

    @Autowired
    BookApplicationService bookApplicationService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    TestDataFactory testDataFactory;

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
        user = testDataFactory.createUser("redkafe@daum.net", "image");
        ReadingLog savedReadingLog = testDataFactory.createReadingLog(user);
        testDataFactory.createBook(savedReadingLog);

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
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.aladin.baseUrl", () -> String.format("http://localhost:%s/", server.getPort()));
        registry.add("api.aladin.ttbkey", () -> "test_key");
    }

    @Test
    void getBookFromAPIWithCacheSuccess() throws JsonProcessingException, InterruptedException {
        String response = objectMapper.writeValueAsString(new AladinResponseData(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        Mono<BookData> firstCall = bookApplicationService.fetchItemFromAladin("9788936434120");

        StepVerifier.create(firstCall)
            .expectNextMatches(book -> book.title().equals("소년이 온다"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");

        Mono<BookData> secondCall = bookApplicationService.fetchItemFromAladin("9788936434120");

        StepVerifier.create(secondCall)
            .expectNextMatches(book -> book.title().equals("소년이 온다"))
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void getBookFromAPIWith500Response() {
        server.enqueue(new MockResponse().setResponseCode(500));
        Mono<BookData> firstCall = bookApplicationService.fetchItemFromAladin("9788936434120");
        StepVerifier.create(firstCall)
            .expectNextCount(0)
            .verifyComplete();

        Mono<BookData> secondCall = bookApplicationService.fetchItemFromAladin("9788936434120");
        StepVerifier.create(secondCall)
            .expectNextCount(0)
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(4);
    }

    @Test
    void getBookListFromAPIWithCache() throws Exception {
        String response = objectMapper.writeValueAsString(new AladinResponseData(1, 1, 1,
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

        Mono<List<SearchedBookData>> firstCall = bookApplicationService.searchBookFromAladin("글쓰기");

        StepVerifier.create(firstCall)
            .expectNextMatches(book -> book.getLast().title().equals("소년이 온다1"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");

        Mono<List<SearchedBookData>> secondCall = bookApplicationService.searchBookFromAladin("글쓰기");

        StepVerifier.create(secondCall)
            .expectNextMatches(book -> book.getLast().title().equals("소년이 온다1"))
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void aopInfo() {
        log.info("isAopProxy, BookService={}", AopUtils.isAopProxy(bookApplicationService));
    }

    @Test
    void getStatus() {
        StatusData status = bookApplicationService.getStatus("9788991290402");
        assertThat(status.status()).isEqualTo("READING");
    }

    @Test
    void getStatusNotExists() {
        StatusData status = bookApplicationService.getStatus("test");
        assertThat(status.status()).isEqualTo("EMPTY");
    }

    @Test
    void getLibrary() {
        List<BookListData> library = bookApplicationService.getLibrary("READING");
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().title()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getLibraryEmpty() {
        List<BookListData> library = bookApplicationService.getLibrary("FINISHED");
        assertThat(library).isEmpty();
    }

    @Test
    void saveOrUpdateBookWithStatus() {
        BookSaveCommand command = new BookSaveCommand(
            "t", "a", "p", "d", "i", "c", "cn", "p", "READING");

        bookApplicationService.saveBook(command);
        ReadingLog readingLog = readingLogRepository.find("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("READING");
    }

    @Test
    void saveOrUpdateBookWithNullStatus() {
        BookSaveCommand command = new BookSaveCommand(
            "t", "a", "p", "d", "i", "c", "cn", "p", null);

        bookApplicationService.saveBook(command);
        ReadingLog readingLog = readingLogRepository.find("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("NEW");
    }

    @Test
    void saveOrUpdateBookWithBlankStatus() {
        BookSaveCommand command = new BookSaveCommand(
            "t", "a", "p", "d", "i", "c", "cn", "p", "");
        bookApplicationService.saveBook(command);
        ReadingLog readingLog = readingLogRepository.find("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("READING");
    }

    @Test
    void changeStatus() {
        BookSaveCommand command = new BookSaveCommand(
            "펠로폰네소스 전쟁사", "투퀴디데스", "2011-06-30", "투퀴디세스가 집필한 전쟁사", "9788991290402", "image",
            "서양고대사", "도서출판 숲", "FINISHED");

        bookApplicationService.saveBook(command);
        ReadingLog readingLog = readingLogRepository.find("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void changeReadingLog() {
        var saveRequest = new ReadingLogUpdateRequestDTO(
            "9788991290402", "note1", "readingLog test");
        bookApplicationService.changeReadingLog(saveRequest);
        Book book = bookApplicationService.find("9788991290402").orElseThrow();

        assertThat(book.getReadingLog().getNote1()).isEqualTo("readingLog test");
    }

    @Transactional
    @Test
    void changeStatusTest() {
        String isbn = "9788991290402";
        String status = "FINISHED";

        bookApplicationService.changeStatus(isbn, status);
        Book book = bookApplicationService.find(isbn).orElseThrow();

        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void saveRating() {
        ReadingLog readingLog = readingLogRepository.find("9788991290402",
            "redkafe@daum.net").orElseThrow();
        readingLog.changeRating(1);
        assertThat(readingLog.getRating()).isEqualTo(1);

        ReadingLog updated = readingLogRepository.find("9788991290402",
            "redkafe@daum.net").orElseThrow();
        assertThat(updated.getRating()).isEqualTo(1);
    }

    @Test
    void getStatusCacheTest() {
        bookApplicationService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        bookApplicationService.getStatus("9788991290402");
        String cachedStatus = cache.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(cachedStatus).isEqualTo("READING");
    }

    @Transactional
    @Test
    void statusUpdateCacheTest() {
        bookApplicationService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        bookApplicationService.getStatus("9788991290402");
        String cachedStatus = cache.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(cachedStatus).isEqualTo("READING");

        bookApplicationService.changeStatus("9788991290402", "FINISHED");

        StatusData status = bookApplicationService.getStatus("9788991290402");
        assertThat(status.status()).isEqualTo("FINISHED");
        Cache cache1 = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        String c = cache1.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(c).isEqualTo("FINISHED");
    }
}
