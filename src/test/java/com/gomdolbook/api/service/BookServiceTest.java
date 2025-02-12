package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookSearchResponseDTO;
import com.gomdolbook.api.api.dto.LibraryResponseDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
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
    private JwtDecoder jwtDecoder;

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
        User user = new User("redkafe@daum.net", "img", Role.USER);
        userRepository.save(user);
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLog.setUser(user);
        ReadingLog savedReadingLog = readingLogRepository.save(readingLog);
        Book mockBook = Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
        mockBook.setReadingLog(savedReadingLog);
        bookRepository.save(mockBook);
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

    @Transactional
    @Test
    void getReadingLog() {
        User user = new User("user", "img", Role.USER);
        userRepository.save(user);
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLogRepository.save(readingLog);
        readingLog.setUser(user);
        Book book = Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
        book.setReadingLog(readingLog);
        bookRepository.save(book);

        BookAndReadingLogDTO bookAndReadingLogDTO = bookService.getReadingLog("9788991290402");
        assertThat(bookAndReadingLogDTO.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(user.getReadingLogs().getFirst().getNote1()).isEqualTo("1");
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

        Mono<List<BookSearchResponseDTO>> list = bookService.searchBookFromAladin("글쓰기");

        StepVerifier.create(list)
            .expectNextMatches(book -> book.getLast().title().equals("소년이 온다1"))
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

    @Test
    void getLibrary() {
        List<LibraryResponseDTO> library = bookService.getLibrary("READING");
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().title()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getLibraryEmpty() {
        List<LibraryResponseDTO> library = bookService.getLibrary("FINISHED");
        assertThat(library).isEmpty();
    }
}
