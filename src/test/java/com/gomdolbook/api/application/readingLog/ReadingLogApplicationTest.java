package com.gomdolbook.api.application.readingLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.readingLog.command.ChangeNoteCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryCommand;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadingLogApplicationTest {

    @InjectMocks
    private ReadingLogApplicationService readingLogApplicationService;

    @Mock
    private ReadingLogRepository readingLogRepository;

    @Mock
    private SecurityService securityService;

    private final String email = "test@email.com";

    @Test
    void getReadingLog() {
        ReadingLogWithBookDTO readingLogWithBookDTO = new ReadingLogWithBookDTO(1L, "title", "author", "cover",
            "publisher", Status.READING,
            "summary", "note", 5,
            LocalDateTime.now(), LocalDateTime.now());

        Long id = readingLogWithBookDTO.id();

        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findWithBookByIdAndEmail(id,
            securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(readingLogWithBookDTO));

        ReadingLogWithBookDTO dto = readingLogApplicationService.getReadingLog(id);

        assertThat(dto.title()).isEqualTo("title");
        verify(readingLogRepository).findWithBookByIdAndEmail(id, email);
    }

    @Test
    void getReadingLog_withNonExistingIsbn_throwException() {
        Long id = 1L;
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findWithBookByIdAndEmail(id,
            securityService.getUserEmailFromSecurityContext())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingLogApplicationService.getReadingLog(id)).isInstanceOf(
            BookNotFoundException.class);
    }

    @Test
    void changeSummary() {
        String summary = "summary";
        ReadingLog readingLog = ReadingLog.of();
        ChangeSummaryCommand changeSummaryCommand = new ChangeSummaryCommand(summary, readingLog.getId(),
            email);

        when(readingLogRepository.findByIdAndEmail(readingLog.getId(), email)).thenReturn(
            Optional.of(readingLog));

        readingLogApplicationService.changeSummary(changeSummaryCommand);

        verify(readingLogRepository).findByIdAndEmail(readingLog.getId(), email);
        assertThat(readingLog.getSummary()).isEqualTo(summary);
    }

    @Test
    void changeSummary_withNonExistId_throwException() {
        String summary = "summary";
        ReadingLog readingLog = ReadingLog.of();
        ChangeSummaryCommand changeSummaryCommand = new ChangeSummaryCommand(summary, readingLog.getId(),
            email);
        when(readingLogRepository.findByIdAndEmail(readingLog.getId(), email)).thenReturn(
            Optional.empty());

        assertThatThrownBy(
            () -> readingLogApplicationService.changeSummary(changeSummaryCommand)).isInstanceOf(
            BookNotFoundException.class);
    }

    @Test
    void changeNote() {
        String note = "note";
        ReadingLog readingLog = ReadingLog.of();
        ChangeNoteCommand changeNoteCommand = new ChangeNoteCommand(note, readingLog.getId(),
            email);

        when(readingLogRepository.findByIdAndEmail(readingLog.getId(), email)).thenReturn(
            Optional.of(readingLog));

        readingLogApplicationService.changeNote(changeNoteCommand);

        verify(readingLogRepository).findByIdAndEmail(readingLog.getId(), email);
        assertThat(readingLog.getNote()).isEqualTo(note);
    }

    @Test
    void changeNote_withNonExistId_throwException() {
        String note = "note";
        ReadingLog readingLog = ReadingLog.of();
        ChangeNoteCommand changeNoteCommand = new ChangeNoteCommand(note, readingLog.getId(),
            email);
        when(readingLogRepository.findByIdAndEmail(readingLog.getId(), email)).thenReturn(
            Optional.empty());

        assertThatThrownBy(
            () -> readingLogApplicationService.changeNote(changeNoteCommand)).isInstanceOf(
            BookNotFoundException.class);
    }

    @Test
    void changeRating() {
        int rating = 5;
        ReadingLog readingLog = ReadingLog.of();
        when(readingLogRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.of(readingLog));
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);

        readingLogApplicationService.changeRating(rating, 1L);

        verify(readingLogRepository).findByIdAndEmail(1L, email);
        assertThat(readingLog.getRating()).isEqualTo(rating);
    }

    @Test
    void changeRating_withNonExistId_throwException() {
        int rating = 5;
        when(readingLogRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.empty());
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);

        assertThatThrownBy(
            () -> readingLogApplicationService.changeRating(rating, 1L)).isInstanceOf(
            BookNotFoundException.class);
    }
}
