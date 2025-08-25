package com.gomdolbook.api.application.readingLog;

import com.gomdolbook.api.application.readingLog.command.ChangeNoteCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryCommand;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.common.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class ReadingLogApplicationService {

    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;
    private final SecurityService securityService;

    @Cacheable(cacheNames = "readingLogCache", keyGenerator = "customKeyGenerator", unless = "#result == null")
    @UserCheckAndSave
    @Transactional(readOnly = true)
    public ReadingLogWithBookDTO getReadingLog(Long id) {
        return readingLogRepository.findWithBookByIdAndEmail(id,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException("Can't find book: " + id));
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#id)")
    @Transactional
    public void changeRating(int rating, Long id) {
        ReadingLog readingLog = readingLogRepository.findByIdAndEmail(id,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new BookNotFoundException(String.valueOf(id)));

        readingLog.changeRating(rating);
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#command.id())")
    @Transactional
    public void changeSummary(ChangeSummaryCommand command) {
        ReadingLog readingLog = readingLogRepository.findByIdAndEmail(command.id(),
                command.email())
            .orElseThrow(() -> new BookNotFoundException(String.valueOf(command.id())));
        readingLog.changeSummary(command.summary());
    }

    @CacheEvict(cacheNames = "readingLogCache", key = "@securityService.getCacheKey(#command.id())")
    @Transactional
    public void changeNote(ChangeNoteCommand command) {
        ReadingLog readingLog = readingLogRepository.findByIdAndEmail(command.id(),
                command.email())
            .orElseThrow(() -> new BookNotFoundException(String.valueOf(command.id())));
        readingLog.changeNote(command.note());
    }
}
