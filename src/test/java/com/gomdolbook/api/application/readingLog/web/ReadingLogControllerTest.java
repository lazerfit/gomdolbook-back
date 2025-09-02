package com.gomdolbook.api.application.readingLog.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.readingLog.ReadingLogApplicationService;
import com.gomdolbook.api.application.readingLog.command.ChangeNoteHandler;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryHandler;
import com.gomdolbook.api.application.readingLog.command.RatingUpdateHandler;
import com.gomdolbook.api.application.readingLog.dto.ChangeNoteRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ChangeRatingRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ChangeSummaryRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.services.SecurityService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockCustomUser
@WebMvcTest(ReadingLogController.class)
class ReadingLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReadingLogApplicationService readingLogApplicationService;
    @MockitoBean
    private SecurityService securityService;
    @MockitoBean
    private RatingUpdateHandler ratingUpdateHandler;
    @MockitoBean
    private ChangeSummaryHandler changeSummaryHandler;
    @MockitoBean
    private ChangeNoteHandler changeNoteHandler;

    @Test
    void getReadingLog() throws Exception {
        ReadingLogWithBookDTO dto = new ReadingLogWithBookDTO(1L, "t", "a", "c", "p",
            "s", "n", 5, LocalDateTime.now(), LocalDateTime.now());
        given(readingLogApplicationService.getReadingLog(1L)).willReturn(dto);

        mockMvc.perform(get("/v1/readingLog/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("t"));
    }

    @Test
    void changeSummary() throws Exception {
        ChangeSummaryRequestDTO dto = new ChangeSummaryRequestDTO("ss");
        mockMvc.perform(patch("/v1/readingLog/{id}/summary", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void changeNote() throws Exception {
        ChangeNoteRequestDTO dto = new ChangeNoteRequestDTO("ss");
        mockMvc.perform(patch("/v1/readingLog/{id}/note", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void changeRating() throws Exception {
        ChangeRatingRequestDTO dto = new ChangeRatingRequestDTO(5);
        mockMvc.perform(patch("/v1/readingLog/{id}/rating", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }
}
