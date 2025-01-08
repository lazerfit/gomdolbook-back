package com.gomdolbook.api.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.dto.AladinAPI;
import com.gomdolbook.api.dto.AladinAPI.Item;
import com.gomdolbook.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.models.Book;
import com.gomdolbook.api.models.ReadingLog;
import com.gomdolbook.api.models.ReadingLog.Status;
import com.gomdolbook.api.repository.BookRepository;
import com.gomdolbook.api.repository.ReadingLogRepository;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
class BookControllerTest {

    static MockWebServer server;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    WebTestClient webTestClient;

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

    @BeforeEach
    void setup() {
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLogRepository.save(readingLog);
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
        book.addReadingLog(readingLog);
        bookRepository.save(book);
    }

    @AfterEach
    void teardown() {
        bookRepository.deleteAll();
        readingLogRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.aladin.baseUrl", () -> String.format("http://localhost:%s/", server.getPort()));
        registry.add("api.aladin.ttbkey", () -> "test_key");
    }

    @Test
    void fetchItemFromAladin() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(new AladinAPI(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        webTestClient.get().uri("/api/v1/book/9788936434120")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("소년이 온다");
    }


    @Test
    void getReadingLog() throws Exception {
        mockMvc.perform(get("/api/v1/readingLog/9788991290402")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("펠로폰네소스 전쟁사"))
            .andExpect(jsonPath("$.author").value("투퀴디데스"))
            .andDo(print());

    }

    @Test
    void getReadingLog_return_error() throws Exception {
        mockMvc.perform(get("/api/v1/readingLog/111")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveBook() throws Exception {
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

        mockMvc.perform(post("/api/v1/book/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isNoContent())
            .andDo(print());
    }

    @Test
    void saveBook_request_null_error() throws Exception {
        BookSaveRequestDTO requestDTO = BookSaveRequestDTO.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher(null)
            .status("READING")
            .build();

        mockMvc.perform(post("/api/v1/book/save")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.errors").value("publisher: must not be blank"))
            .andDo(print());

    }

    @Test
    void HttpRequestMethodNotSupportedException() throws Exception {
        mockMvc.perform(post("/api/v1/readingLog/11"))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.errors").value(
                "POST method is not supported for this request. Supported methods are GET "))
            .andDo(print());
    }

    @Test
    void DefaultHandler() throws Exception {
        mockMvc.perform(get("/api/v1/readingLog/"))
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.errors").value("error occurred"))
            .andDo(print());
    }

    @Test
    void BookNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/readingLog/1234"))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.errors").value("Can't find Book: 1234"))
            .andDo(print());
    }
}
