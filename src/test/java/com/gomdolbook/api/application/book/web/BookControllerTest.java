package com.gomdolbook.api.application.book.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.BookSaveHandler;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.application.readingLog.command.StatusUpdateHandler;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book.Status;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockCustomUser
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookApplicationService bookApplicationService;

    @MockitoBean
    private BookSaveHandler bookSaveHandler;

    @MockitoBean
    private StatusUpdateHandler statusUpdateHandler;

    @Test
    void saveBook() throws Exception {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");

        mockMvc.perform(post("/v1/book/save")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookSaveCommand))
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    void getLibrary() throws Exception {
        BookListData bookListData = new BookListData("c", "t", "i", Status.READING, 1L);

        given(bookApplicationService.getLibrary("READING"))
            .willReturn(List.of(bookListData));

        mockMvc.perform(get("/v1/book/Library")
                .param("status", "READING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].isbn").value("i"));
    }

    @Test
    void getLibrary_empty() throws Exception {
        given(bookApplicationService.getLibrary("READING"))
            .willReturn(List.of());

        mockMvc.perform(get("/v1/book/Library")
                .param("status", "READING"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getFinishedBookCalendar() throws Exception {
        FinishedBookCalendarData data = new FinishedBookCalendarData("t", "i",
            "c", 5, LocalDateTime.now());

        given(bookApplicationService.getFinishedBookCalendarData()).willReturn(List.of(data));

        mockMvc.perform(get("/v1/book/calendar/finished"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("t"));
    }

    @Test
    void getFinishedBookCalendar_empty() throws Exception {
        given(bookApplicationService.getFinishedBookCalendarData()).willReturn(List.of());

        mockMvc.perform(get("/v1/book/calendar/finished"))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus() throws Exception {
        mockMvc.perform(patch("/v1/book/status/{isbn}", "isbn")
                .param("status", "READING")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    void getStatus() throws Exception {
        given(bookApplicationService.getStatus("isbn")).willReturn(StatusData.of("READING"));

        mockMvc.perform(get("/v1/book/status/{isbn}", "isbn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("READING"));
    }

}
