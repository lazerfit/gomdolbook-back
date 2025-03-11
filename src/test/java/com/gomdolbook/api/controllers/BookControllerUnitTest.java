package com.gomdolbook.api.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.api.controllers.BookController;
import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.BookSearchResponseDTO;
import com.gomdolbook.api.api.dto.ReadingLogUpdateRequestDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.service.Auth.SecurityService;
import com.gomdolbook.api.service.BookService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

@WithMockCustomUser
@WebMvcTest(BookController.class)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
class BookControllerUnitTest {

    @MockitoBean
    BookService bookService;

    @MockitoBean
    SecurityService securityService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getReadingLog() throws Exception {
        BookAndReadingLogDTO dto = new BookAndReadingLogDTO("펠로폰네소스 전쟁사", "투퀴디데스", "2011-06-30",
            "image", "도서출판 숲",
            Status.READING, "1", "2", "3", 1);

        Mockito.when(bookService.getReadingLog("redkafe@daum.net", "testIsbn")).thenReturn(dto);

        mockMvc.perform(get("/v1/readingLog")
                .param("isbn", "testIsbn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.rating").value(1))
            .andDo(print());
    }

    @Test
    void getBookFromAPI() {
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
        BookDTO bookDTO = new BookDTO(book);

        Mockito.when(bookService.fetchItemFromAladin("isbn")).thenReturn(Mono.just(bookDTO));

        webTestClient.get().uri("/v1/book/isbn")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.title").isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void searchBook() {
        List<BookSearchResponseDTO> dtoList = List.of(BookSearchResponseDTO.builder()
            .title("소년이 온다")
            .author("한강")
            .isbn13("isbn")
            .cover("img")
            .pubDate("2014-05-19")
            .description("2024 노벨문학상")
            .publisher("창비")
            .build(), BookSearchResponseDTO.builder()
            .title("소년이 온다1")
            .author("한강1")
            .isbn13("isbn1")
            .cover("igm1")
            .pubDate("2014-05-191")
            .description("2024 노벨문학상1")
            .publisher("창비1")
            .build());

        Mockito.when(bookService.searchBookFromAladin("글쓰기")).thenReturn(Mono.just(dtoList));

        webTestClient.get().uri(uriBuilder -> uriBuilder
            .path("/v1/book/search")
            .queryParam("q", "글쓰기").build())
            .exchange()
            .expectBody()
            .jsonPath("$.data[0].title").isEqualTo("소년이 온다");
    }

    @Test
    void getStatus() throws Exception {
        Mockito.when(bookService.getStatus("isbn")).thenReturn("NEW");

        mockMvc.perform(get("/v1/status/isbn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("NEW"))
            .andDo(print());
    }

    @Test
    void getLibrary() throws Exception {
        BookListResponseDTO dto1 = new BookListResponseDTO("img", "title1", "isbn", Status.READING);
        BookListResponseDTO dto2 = new BookListResponseDTO("img2", "title2", "isbn", Status.READING);
        List<BookListResponseDTO> list = List.of(dto1, dto2);

        Mockito.when(bookService.getLibrary("READING")).thenReturn(list);

        mockMvc.perform(get("/v1/book/Library").param("status", "READING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("title1"))
            .andDo(print());
    }

    @Test
    void getLibraryEmpty() throws Exception {
        Mockito.when(bookService.getLibrary("READING")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/book/Library").param("status", "READING"))
            .andExpect(status().isNoContent())
            .andDo(print());
    }

    @Test
    void saveReadingLog() throws Exception {
        var saveRequest = new ReadingLogUpdateRequestDTO("9788991290402", "note1", "note1 saved");

        mockMvc.perform(post("/v1/readingLog/save")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveRequest)))
            .andExpect(status().isOk())
            .andDo(print());
    }
}
