package com.gomdolbook.api.application.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.application.book.dto.AladinResponseData;
import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.SearchedBookData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WithMockCustomUser
@Transactional
@SpringBootTest
class BookApplicationServiceTest {

    static MockWebServer server;
    static User user;

    @Autowired
    BookApplicationService bookApplicationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeAll
    static void setServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void serverDown() throws IOException {
        server.shutdown();
    }

    @BeforeEach
    void setup() {
        user = userRepository.save(new User("redkafe@daum.net", "img", Role.USER));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.aladin.baseUrl", () -> String.format("http://localhost:%s/", server.getPort()));
        registry.add("api.aladin.ttbkey", () -> "test_key");
    }

    @Test
    void registerBookWithMeta_nullIsbn_shouldThrowException() {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            null, "cover", "카테고리", "출판사", "READING"
        );
        assertThatThrownBy(() -> bookApplicationService.registerBookWithMeta(command))
            .isInstanceOf(Exception.class);
    }

    @Test
    void registerBookWithMeta_blankStatus_shouldSetStatusNew() {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "9999999999999", "cover", "카테고리", "출판사", ""
        );
        var book = bookApplicationService.registerBookWithMeta(command);
        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("NEW");
    }

    @Test
    void getStatus_withBlankIsbn_shouldReturnEmpty() {
        assertThat(bookApplicationService.getStatus("").status()).isEqualTo("EMPTY");
    }

    @Test
    void getLibrary_withInvalidStatus_shouldThrowException() {
        assertThatThrownBy(() -> bookApplicationService.getLibrary("NOT_A_STATUS"))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void changeStatus_withInvalidStatus_shouldThrowException() {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "9999999999999", "cover", "카테고리", "출판사", ""
        );
        bookApplicationService.registerBookWithMeta(command);

        assertThatThrownBy(() -> bookApplicationService.changeStatus("9999999999999", "INVALID"))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void changeReadingLog_withInvalidNote_shouldThrowException() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "1234567890123", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(bookSaveCommand);

        var readingLogUpdateCommand = new ReadingLogUpdateCommand(
            "1234567890123", "invalidNote", "text"
        );
        assertThatThrownBy(() -> bookApplicationService.changeReadingLog(readingLogUpdateCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void find_withNonexistentIsbn_shouldReturnEmptyOptional() {
        Optional<Book> result = bookApplicationService.find("not_exist_isbn");
        assertThat(result).isEmpty();
    }

    @Test
    void getBookFromAPI_WithCacheSuccess() throws JsonProcessingException, InterruptedException {
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
    void searchBookFromAladin_success() throws Exception {
        String response = objectMapper.writeValueAsString(new AladinResponseData(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                    "9788936434120", "image1", "노벨문학상",
                    "창비"),
                new Item("제목", "저자", "2025-01-01", "설명",
                    "1234567890123", "cover", "카테고리", "출판사"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        StepVerifier.create(bookApplicationService.searchBookFromAladin("소년이 온다"))
            .assertNext(list -> {
                SearchedBookData first = list.getFirst();
                SearchedBookData last = list.getLast();
                assertThat(list).hasSize(2);
                assertThat(first.title()).isEqualTo("소년이 온다");
                assertThat(last.title()).isEqualTo("제목");
            })
            .verifyComplete();
    }
}
