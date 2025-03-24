package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import com.gomdolbook.api.api.dto.StatusDTO;
import com.gomdolbook.api.api.dto.book.BookDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.book.BookSearchResponseDTO;
import com.gomdolbook.api.api.dto.ReadingLogUpdateRequestDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
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
class BookServiceTest {

    static MockWebServer server;
    static User user;

    @Autowired
    BookService bookService;

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
        String response = objectMapper.writeValueAsString(new AladinAPI(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        Mono<BookDTO> firstCall = bookService.fetchItemFromAladin("9788936434120");

        StepVerifier.create(firstCall)
            .expectNextMatches(book -> book.title().equals("소년이 온다"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");

        Mono<BookDTO> secondCall = bookService.fetchItemFromAladin("9788936434120");

        StepVerifier.create(secondCall)
            .expectNextMatches(book -> book.title().equals("소년이 온다"))
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void getBookFromAPIWith500Response() {
        server.enqueue(new MockResponse().setResponseCode(500));
        Mono<BookDTO> firstCall = bookService.fetchItemFromAladin("9788936434120");
        StepVerifier.create(firstCall)
            .expectNextCount(0)
            .verifyComplete();

        Mono<BookDTO> secondCall = bookService.fetchItemFromAladin("9788936434120");
        StepVerifier.create(secondCall)
            .expectNextCount(0)
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(4);
    }

    @Test
    void getBookListFromAPIWithCache() throws Exception {
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

        Mono<List<BookSearchResponseDTO>> firstCall = bookService.searchBookFromAladin("글쓰기");

        StepVerifier.create(firstCall)
            .expectNextMatches(book -> book.getLast().title().equals("소년이 온다1"))
            .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");

        Mono<List<BookSearchResponseDTO>> secondCall = bookService.searchBookFromAladin("글쓰기");

        StepVerifier.create(secondCall)
            .expectNextMatches(book -> book.getLast().title().equals("소년이 온다1"))
            .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void aopInfo() {
        log.info("isAopProxy, BookService={}", AopUtils.isAopProxy(bookService));
    }

    @Test
    void getStatus() {
        StatusDTO status = bookService.getStatus("9788991290402");
        assertThat(status.status()).isEqualTo("READING");
    }

    @Test
    void getStatusNotExists() {
        StatusDTO status = bookService.getStatus("test");
        assertThat(status.status()).isEqualTo("EMPTY");
    }

    @Test
    void getLibrary() {
        List<BookListResponseDTO> library = bookService.getLibrary("READING");
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().title()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getLibraryEmpty() {
        List<BookListResponseDTO> library = bookService.getLibrary("FINISHED");
        assertThat(library).isEmpty();
    }

    @Test
    void saveOrUpdateBookWithStatus() {
        BookSaveRequestDTO bookSaveRequestDTO = new BookSaveRequestDTO(
            "t", "a", "p", "d", "i", "c", "cn", "p", "READING");

        bookService.saveBook(bookSaveRequestDTO);
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("READING");
    }

    @Test
    void saveOrUpdateBookWithNullStatus() {
        BookSaveRequestDTO bookSaveRequestDTO = new BookSaveRequestDTO(
            "t", "a", "p", "d", "i", "c", "cn", "p", null);

        bookService.saveBook(bookSaveRequestDTO);
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("NEW");
    }

    @Test
    void saveOrUpdateBookWithBlankStatus() {
        BookSaveRequestDTO bookSaveRequestDTO = new BookSaveRequestDTO(
            "t", "a", "p", "d", "i", "c", "cn", "p", "");
        bookService.saveBook(bookSaveRequestDTO);
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("READING");
    }

    @Test
    void updateStatus() {
        BookSaveRequestDTO bookSaveRequestDTO = new BookSaveRequestDTO(
            "펠로폰네소스 전쟁사", "투퀴디데스", "2011-06-30", "투퀴디세스가 집필한 전쟁사", "9788991290402", "image",
            "서양고대사", "도서출판 숲", "FINISHED");

        bookService.saveBook(bookSaveRequestDTO);
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("i", "redkafe@daum.net")
            .orElseThrow();
        assertThat(readingLog.getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void updateReadingLog() {
        var saveRequest = new ReadingLogUpdateRequestDTO(
            "9788991290402", "note1", "readingLog test");
        bookService.updateReadingLog(saveRequest);
        Book book = bookService.findByIsbn("9788991290402").orElseThrow();

        assertThat(book.getReadingLog().getNote1()).isEqualTo("readingLog test");
    }

    @Transactional
    @Test
    void updateStatusTest() {
        String isbn = "9788991290402";
        String status = "FINISHED";

        bookService.updateStatus(isbn, status);
        Book book = bookService.findByIsbn(isbn).orElseThrow();

        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void saveRating() {
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("9788991290402",
            "redkafe@daum.net").orElseThrow();
        readingLog.updateRating(1);
        assertThat(readingLog.getRating()).isEqualTo(1);

        ReadingLog updated = readingLogRepository.findByIsbnAndEmail("9788991290402",
            "redkafe@daum.net").orElseThrow();
        assertThat(updated.getRating()).isEqualTo(1);
    }

    @Test
    void getStatusCacheTest() {
        bookService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        bookService.getStatus("9788991290402");
        String cachedStatus = cache.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(cachedStatus).isEqualTo("READING");
    }

    @Transactional
    @Test
    void statusUpdateCacheTest() {
        bookService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        bookService.getStatus("9788991290402");
        String cachedStatus = cache.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(cachedStatus).isEqualTo("READING");

        bookService.updateStatus("9788991290402", "FINISHED");

        StatusDTO status = bookService.getStatus("9788991290402");
        assertThat(status.status()).isEqualTo("FINISHED");
        Cache cache1 = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        String c = cache1.get("redkafe@daum.net:[9788991290402]", String.class);
        assertThat(c).isEqualTo("FINISHED");
    }
}
