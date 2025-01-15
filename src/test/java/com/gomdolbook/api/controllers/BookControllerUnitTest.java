package com.gomdolbook.api.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gomdolbook.api.api.controllers.BookController;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.ReadingLogDTO;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;


@WebMvcTest(BookController.class)
@AutoConfigureWebTestClient
class BookControllerUnitTest {

    @MockitoBean
    BookService bookService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void getReadingLog() throws Exception {
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
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
        ReadingLogDTO readingLogDTO = new ReadingLogDTO(book);
        Mockito.when(bookService.getReadingLog("testIsbn")).thenReturn(readingLogDTO);

        mockMvc.perform(get("/api/v1/readingLog/testIsbn")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("펠로폰네소스 전쟁사"))
            .andExpect(jsonPath("$.data.status").value("READING"))
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

        webTestClient.get().uri("/api/v1/book/isbn")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.title").isEqualTo("펠로폰네소스 전쟁사");

    }

    @Test
    void getStatus() throws Exception {
        Mockito.when(bookService.getStatus("isbn")).thenReturn("NEW");

        mockMvc.perform(get("/api/v1/status/isbn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("NEW"))
            .andDo(print());
    }

}
