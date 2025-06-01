package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.application.book.dto.AladinResponseData;
import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Transactional
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
    BookMetaRepository bookMetaRepository;

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

    @Transactional
    @Test
    void registerBookWithMeta_createsBookAndMetaAndReadingLog() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "READING"
        );

        Book book = bookApplicationService.registerBookWithMeta(command);

        BookMeta meta = bookMetaRepository.findByIsbn("1234567890123").orElse(null);
        assertThat(meta).isNotNull();
        assertThat(meta.getBooks()).contains(book);

        assertThat(book.getBookMeta()).isEqualTo(meta);
        assertThat(book.getReadingLog()).isNotNull();
        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("READING");
        assertThat(book.getStartedAt()).isNotNull();
        assertThat(book.getFinishedAt()).isNull();
    }

    @Transactional
    @Test
    void registerBookWithMeta_statusFinished_setsFinishedAt() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책2", "저자2", "2025-01-02", "설명2",
            "2234567890123", "cover2", "카테고리2", "출판사2", "FINISHED"
        );

        Book book = bookApplicationService.registerBookWithMeta(command);

        assertThat(book.getFinishedAt()).isNotNull();
        assertThat(book.getStartedAt()).isNull();
        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void registerBookWithMeta_invalidStatus_shouldThrowException() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "INVALID"
        );

        assertThatThrownBy(() -> bookApplicationService.registerBookWithMeta(command))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("잘못된 Status 값입니다");
    }

    @Transactional
    @Test
    void registerBookWithMeta_existingBookMeta_shouldNotDuplicateMeta() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        Book book2 = bookApplicationService.registerBookWithMeta(command);

        List<BookMeta> metas = bookMetaRepository.findAll();
        assertThat(metas).hasSize(1);
        assertThat(metas.getFirst().getBooks()).contains(book2);
    }

    @Transactional
    @Test
    void registerBookWithMeta_statusNull_shouldSetStatusNew() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", null
        );

        Book book = bookApplicationService.registerBookWithMeta(command);

        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("NEW");
    }

    @Transactional
    @Test
    void registerBookWithMeta_sameIsbn_differentUsers_shouldShareBookMetaButHaveSeparateBooksAndLogs() {
        BookSaveCommand command = new BookSaveCommand(
            "테스트책", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "READING"
        );

        Book book1 = bookApplicationService.registerBookWithMeta(command);

        SimpleGrantedAuthority roleUser = new SimpleGrantedAuthority("ROLE_USER");
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("authorities", List.of("user"))
            .claim("email", "redkafe1@daum.net")
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(jwt, null, List.of(roleUser))
        );
        Book book2 = bookApplicationService.registerBookWithMeta(command);

        List<BookMeta> metas = bookMetaRepository.findAll();
        assertThat(metas).hasSize(1);
        assertThat(book1.getBookMeta()).isSameAs(book2.getBookMeta());

        assertThat(book1).isNotEqualTo(book2);
        assertThat(book1.getReadingLog()).isNotEqualTo(book2.getReadingLog());
        assertThat(book1.getReadingLog().getUser().getEmail()).isEqualTo("redkafe@daum.net");
        assertThat(book2.getReadingLog().getUser().getEmail()).isEqualTo("redkafe1@daum.net");
    }

    @Transactional
    @Test
    void registerBookWithMeta_existingIsbn_differentFields_shouldNotUpdateBookMeta() {
        BookSaveCommand command1 = new BookSaveCommand(
            "제목1", "저자1", "2025-01-01", "설명1",
            "1234567890123", "cover1", "카테고리1", "출판사1", "READING"
        );
        BookSaveCommand command2 = new BookSaveCommand(
            "다른제목", "다른저자", "2026-01-01", "다른설명",
            "1234567890123", "다른커버", "다른카테고리", "다른출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command1);
        Book book2 = bookApplicationService.registerBookWithMeta(command2);

        BookMeta meta = bookMetaRepository.findByIsbn("1234567890123").orElseThrow();
        assertThat(meta.getTitle()).isEqualTo("제목1");
        assertThat(meta.getAuthor()).isEqualTo("저자1");
        assertThat(meta.getBooks()).contains(book2);
    }

    @Transactional
    @Test
    void registerBookWithMeta_missingRequiredField_shouldThrowException() {
        BookSaveCommand command = new BookSaveCommand(
            null, "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "READING"
        );
        assertThatThrownBy(() -> bookApplicationService.registerBookWithMeta(command))
            .isInstanceOf(Exception.class);
    }

    @Transactional
    @Test
    void changeStatus_shouldEvictStatusCache() {
        BookSaveCommand command = new BookSaveCommand(
            "캐시책", "저자", "2025-01-01", "설명",
            "5234567890123", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);
        bookApplicationService.getStatus("5234567890123");
        Cache cache = cacheManager.getCache("statusCache");
        assertThat(cache.get("redkafe@daum.net:[5234567890123]")).isNotNull();

        bookApplicationService.changeStatus("5234567890123", "FINISHED");
        assertThat(cache.get("redkafe@daum.net:[5234567890123]")).isNull();
    }

    @Transactional
    @Test
    void deleteBook_shouldNotDeleteBookMeta() {
        BookSaveCommand command = new BookSaveCommand(
            "삭제책", "저자", "2025-01-01", "설명",
            "6234567890123", "cover", "카테고리", "출판사", "READING"
        );
        Book book = bookApplicationService.registerBookWithMeta(command);
        Long metaId = book.getBookMeta().getId();

        bookRepository.delete(book);
        em.flush();
        em.clear();

        BookMeta meta = bookMetaRepository.findById(metaId).orElse(null);
        assertThat(meta).isNotNull();
    }
}
