package com.gomdolbook.api.application.readingLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.readingLog.command.ChangeNoteCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryCommand;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@Transactional
@SpringBootTest
class ReadingLogApplicationCacheIT {

    @MockitoBean
    ReadingLogRepository readingLogRepository;

    @Autowired
    ReadingLogApplicationService readingLogApplicationService;

    @MockitoBean
    SecurityService securityService;

    @Autowired
    CacheManager cm;

    @AfterEach
    void cleanUp() {
        cm.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cm.getCache(cacheName)).clear());
    }

    private final String email = "test@email.com";
    ReadingLogWithBookDTO readingLogWithBookDTO = new ReadingLogWithBookDTO(1L, "title", "author", "cover",
        "publisher", Status.READING,
        "summary", "note", 5,
        LocalDateTime.now(), LocalDateTime.now());

    @Test
    void getReadingLog_cache() {
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findWithBookByIdAndEmail(1L, email)).thenReturn(
            Optional.of(readingLogWithBookDTO));

        Long readingLogId = 1L;
        ReadingLogWithBookDTO result1 = readingLogApplicationService.getReadingLog(readingLogId);
        ReadingLogWithBookDTO result2 = readingLogApplicationService.getReadingLog(readingLogId);

        verify(readingLogRepository, times(1)).findWithBookByIdAndEmail(readingLogId, email);
        assertThat(result1).isEqualTo(readingLogWithBookDTO);
        assertThat(result2).isEqualTo(readingLogWithBookDTO);
        assertThat(result1).isSameAs(result2);
    }

    @Test
    void changeRating_cacheEvict() {
        ReadingLog readingLog = ReadingLog.of();
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(readingLog));
        when(readingLogRepository.findWithBookByIdAndEmail(1L, email)).thenReturn(
            Optional.of(readingLogWithBookDTO));
        when(securityService.getCacheKey(1L)).thenReturn(
            "test@email.com:1");

        readingLogApplicationService.getReadingLog(1L);
        readingLogApplicationService.changeRating(5, 1L);
        readingLogApplicationService.getReadingLog(1L);

        verify(readingLogRepository, times(2)).findWithBookByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext());
    }

    @Test
    void changeSummary_cacheEvict() {
        ReadingLog readingLog = ReadingLog.of();
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(readingLog));
        when(readingLogRepository.findWithBookByIdAndEmail(1L, email)).thenReturn(
            Optional.of(readingLogWithBookDTO));
        when(securityService.getCacheKey(1L)).thenReturn(
            "test@email.com:1");

        readingLogApplicationService.getReadingLog(1L);
        readingLogApplicationService.changeSummary(new ChangeSummaryCommand("summary", 1L, email));
        readingLogApplicationService.getReadingLog(1L);

        verify(readingLogRepository, times(2)).findWithBookByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext());
    }

    @Test
    void changeNote_cacheEvict() {
        ReadingLog readingLog = ReadingLog.of();
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(readingLogRepository.findByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(readingLog));
        when(readingLogRepository.findWithBookByIdAndEmail(1L, email)).thenReturn(
            Optional.of(readingLogWithBookDTO));
        when(securityService.getCacheKey(1L)).thenReturn(
            "test@email.com:1");

        readingLogApplicationService.getReadingLog(1L);
        readingLogApplicationService.changeNote(new ChangeNoteCommand("note",1L, email));
        readingLogApplicationService.getReadingLog(1L);

        verify(readingLogRepository, times(2)).findWithBookByIdAndEmail(1L,
            securityService.getUserEmailFromSecurityContext());
    }
}
