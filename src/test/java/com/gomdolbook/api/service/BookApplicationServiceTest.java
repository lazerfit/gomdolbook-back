package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.application.book.dto.AladinResponseData;
import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.readingLog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.util.TestDataFactory;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import org.springframework.cache.Cache.ValueWrapper;
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

    @Autowired
    EntityManager em;

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
    }

    @AfterEach
    void teardown() {
        em.clear();
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

    @Transactional
    @Test
    void changeReadingLog() {
        var saveRequest = new ReadingLogUpdateCommand(
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
        em.flush();
        em.clear();
        BookAndReadingLogData readingLog = bookApplicationService.getReadingLog(isbn);
        assertThat(readingLog.getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void saveRating() {
        ReadingLog readingLog = readingLogRepository.findByEmail("9788991290402",
            "redkafe@daum.net").orElseThrow();
        readingLog.changeRating(1);
        assertThat(readingLog.getRating()).isEqualTo(1);

        ReadingLog updated = readingLogRepository.findByEmail("9788991290402",
            "redkafe@daum.net").orElseThrow();
        assertThat(updated.getRating()).isEqualTo(1);
    }

    @Transactional
    @Test
    void getStatusCacheTest() {
        bookApplicationService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        ValueWrapper valueWrapper = cache.get("redkafe@daum.net:[9788991290402]");
        assertThat(valueWrapper).isNotNull();
        Object cachedValue = valueWrapper.get();
        assertThat(cachedValue).isNotNull();

        StatusData secondCallStatus = bookApplicationService.getStatus("9788991290402");

        assertThat(secondCallStatus).isEqualTo(cachedValue);
    }

    @Transactional
    @Test
    void statusUpdateCacheTest() {
        bookApplicationService.getStatus("9788991290402");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache).isNotNull();
        ValueWrapper valueWrapper = cache.get("redkafe@daum.net:[9788991290402]");
        assertThat(valueWrapper).isNotNull();
        Object cachedValue = valueWrapper.get();
        assertThat(cachedValue).isNotNull();

        StatusData secondCallStatus = bookApplicationService.getStatus("9788991290402");

        assertThat(secondCallStatus).isEqualTo(cachedValue);

        bookApplicationService.changeStatus("9788991290402", "FINISHED");

        StatusData status = bookApplicationService.getStatus("9788991290402");
        assertThat(status.status()).isEqualTo("FINISHED");
        Cache cache1 = cacheManager.getCache("statusCache");
        assertThat(cache1).isNotNull();
        ValueWrapper valueWrapper2 = cache1.get("redkafe@daum.net:[9788991290402]");
        assertThat(valueWrapper2).isNotNull();
        Object cachedValue2 = valueWrapper2.get();
        assertThat(cachedValue2).isNotNull();

        StatusData secondCallStatus2 = bookApplicationService.getStatus("9788991290402");

        assertThat(secondCallStatus2).isEqualTo(cachedValue2);
    }

    @Transactional
    @Test
    void saveBookAndValidateStartedAt() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "author", "2024-01-01",
            "description",
            "9788936434120", "cover", "categoryName", "publisher", "READING");

        Book book = Book.of(bookSaveCommand);
        ReadingLog readingLog = ReadingLog.of(user, Status.READING);
        book.setReadingLog(readingLog);
        ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        book.changeStartedAt(kst.toLocalDateTime());
        bookRepository.save(book);

        Book savedBook = bookRepository.findByIsbn("9788936434120").orElseThrow();
        assertThat(savedBook.getStartedAt()).isEqualTo(kst.toLocalDateTime());
    }

    @Transactional
    @Test
    void saveBookAndValidateFinishedAt() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "author", "2024-01-01",
            "description",
            "9788936434120", "cover", "categoryName", "publisher", "FINISHED");

        Book book = Book.of(bookSaveCommand);
        ReadingLog readingLog = ReadingLog.of(user, Status.FINISHED);
        book.setReadingLog(readingLog);
        ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        book.changeFinishedAt(kst.toLocalDateTime());
        bookRepository.save(book);

        Book savedBook = bookRepository.findByIsbn("9788936434120").orElseThrow();
        assertThat(savedBook.getFinishedAt()).isEqualTo(kst.toLocalDateTime());
    }

    @Transactional
    @Test
    void getStartedAtAndNullOfFinishedAt() {
        Book savedBook = bookRepository.findByIsbn("9788991290402").orElseThrow();

        LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        assertThat(savedBook.getStartedAt()).isEqualTo(localDateTime);
        assertThat(savedBook.getFinishedAt()).isNull();
    }

    @Transactional
    @Test
    void getFinishedBookCalendarData() {
        ZonedDateTime kst = createFinishedBookCalendarDataBoilerplate();

        List<FinishedBookCalendarData> list = bookRepository.getFinishedBookCalendarData(
            "redkafe@daum.net");

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getFinishedAt()).isEqualTo(kst.toLocalDate());
    }

    @Transactional
    @Test
    void getFinishedBookCalendarCache() {
        createFinishedBookCalendarDataBoilerplate();

        bookApplicationService.getFinishedBookCalendarData();
        Cache cache = cacheManager.getCache("finishedBookCalendarData");
        assertThat(cache).isNotNull();

        Cache.ValueWrapper wrapper = cache.get("redkafe@daum.net");
        assertThat(wrapper).isNotNull();
        Object cachedValue = wrapper.get();
        assertThat(cachedValue).isNotNull();

        List<FinishedBookCalendarData> secondCall = bookApplicationService.getFinishedBookCalendarData();

        assertThat(secondCall).isEqualTo(cachedValue);
    }

    private ZonedDateTime createFinishedBookCalendarDataBoilerplate() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "author", "2024-01-01",
            "description",
            "9788936434120", "cover", "categoryName", "publisher", "FINISHED");

        Book book = Book.of(bookSaveCommand);
        ReadingLog readingLog = ReadingLog.of(user, Status.FINISHED);
        book.setReadingLog(readingLog);
        ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        book.changeFinishedAt(kst.toLocalDateTime());
        bookRepository.save(book);

        return kst;
    }
}
