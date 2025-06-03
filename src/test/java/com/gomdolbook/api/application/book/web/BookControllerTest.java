package com.gomdolbook.api.application.book.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@WithMockCustomUser
@Transactional
@SpringBootTest
class BookControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BookApplicationService bookApplicationService;

    static User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("redkafe@daum.net", "img", Role.USER));
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "1234567890", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);
    }

    @Test
    void saveBook_정상동작() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "1234567890", "cover", "카테고리", "출판사", "READING"
        );
        mockMvc.perform(post("/v1/book/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isNoContent())
            .andDo(print());
    }

    @Test
    void saveBook_withNullIsbn_error() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            null, "cover", "카테고리", "출판사", "READING"
        );

        mockMvc.perform(post("/v1/book/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("isbn: must not be blank"))
            .andDo(print());
    }

    @Test
    void getReadingLog_정상동작() throws Exception {

        mockMvc.perform(get("/v1/readingLog")
                .param("isbn", "1234567890"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void getReadingLog_invalid_isbn_error() throws Exception {

        mockMvc.perform(get("/v1/readingLog")
                .param("isbn", "123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("Can't find Book: 123"))
            .andDo(print());
    }

    @Test
    void updateReadingLog_정상동작() throws Exception {

        ReadingLogUpdateCommand readingLogUpdateCommand = new ReadingLogUpdateCommand("1234567890",
            "note1", "text");

        mockMvc.perform(post("/v1/readingLog/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingLogUpdateCommand)))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void updateReadingLog_invalid_isbn_error() throws Exception {

        ReadingLogUpdateCommand readingLogUpdateCommand = new ReadingLogUpdateCommand(null,
            "note1", "text");

        mockMvc.perform(post("/v1/readingLog/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingLogUpdateCommand)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("isbn: must not be blank"))
            .andDo(print());
    }

    @Test
    void updateRating_정상동작() throws Exception {
        mockMvc.perform(post("/v1/readingLog/rating/update")
                .param("isbn", "1234567890")
                .param("star", "5"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void updateRating_invalid_isbn_error() throws Exception {
        mockMvc.perform(post("/v1/readingLog/rating/update")
                .param("isbn", "1")
                .param("star", "5"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("Can't find Book: can't find book: 1"))
            .andDo(print());
    }

    @Test
    void updateRating_invalid_star_error() throws Exception {
        mockMvc.perform(post("/v1/readingLog/rating/update")
                .param("isbn", "1234567890")
                .param("star", "50"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("star 값이 비정상적입니다."))
            .andDo(print());
    }

    @Test
    void getStatus_정상동작() throws Exception {
        mockMvc.perform(get("/v1/status/1234567890"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void getStatus_invalid_isbn_return_empty() throws Exception {
        mockMvc.perform(get("/v1/status/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("EMPTY"))
            .andDo(print());
    }

    @Test
    void updateStatus_정상동작() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(post("/v1/status/isbn/update")
                .param("status", "FINISHED"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void updateStatus_invalid_status_error() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(post("/v1/status/isbn/update")
                .param("status", "INVALID"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andDo(print());
    }

    @Test
    void updateStatus_invalid_isbn_error() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(post("/v1/status/isbn1234/update")
                .param("status", "FINISHED"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andDo(print());
    }

    @Test
    void getLibrary_정상동작() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(get("/v1/book/Library")
                .param("status", "READING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("제목"))
            .andDo(print());
    }

    @Test
    void getLibrary_invalid_status_error() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(get("/v1/book/Library")
                .param("status", "INVALID"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andDo(print());
    }

    @Test
    void getFinishedBookCalendar_정상동작() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "FINISHED"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(get("/v1/book/calendar/finished"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void getFinishedBookCalendar_noContent() throws Exception {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        bookApplicationService.registerBookWithMeta(command);

        mockMvc.perform(get("/v1/book/calendar/finished"))
            .andExpect(status().isNoContent())
            .andDo(print());
    }
}
