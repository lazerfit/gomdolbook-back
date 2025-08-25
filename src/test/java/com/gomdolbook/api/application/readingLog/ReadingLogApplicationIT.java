package com.gomdolbook.api.application.readingLog;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeNoteCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryCommand;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@Transactional
@SpringBootTest
class ReadingLogApplicationIT {

    @Autowired
    private ReadingLogApplicationService readingLogApplicationService;

    @Autowired
    private BookApplicationService bookApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReadingLogRepository readingLogRepository;

    @Autowired
    private EntityManager em;

    private final String email = "test@email.com";
    private Long readingLogId;

    @BeforeEach
    void setUp() {
        userRepository.save(new User(email, "pic", Role.USER));

        String isbn = "isbn-1234";
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", isbn, "c", "ca",
            "pu", "READING");

        bookApplicationService.addBookToLibrary(bookSaveCommand);

        em.flush();
        em.clear();

        ReadingLogWithBookDTO dto = readingLogRepository.findWithBookByIsbnAndEmail(
                isbn, email)
            .orElseThrow(() -> new AssertionError("Can't find readingLog"));

        readingLogId = dto.id();
    }

    @Test
    void changeSummary_updateSummaryInDB() {
        String summary = "newSummary";

        ChangeSummaryCommand changeSummaryCommand = new ChangeSummaryCommand(summary, readingLogId,
            email);

        readingLogApplicationService.changeSummary(changeSummaryCommand);

        em.flush();
        em.clear();

        ReadingLog foundReadingLog = readingLogRepository.findByIdAndEmail(readingLogId, email)
            .orElseThrow(() -> new AssertionError("ReadingLog not found"));

        assertThat(foundReadingLog.getSummary()).isEqualTo(summary);
    }

    @Test
    void changeNote_updateNoteInDB() {
        String note = "newNote";

        ChangeNoteCommand changeNoteCommand = new ChangeNoteCommand(note, readingLogId, email);
        readingLogApplicationService.changeNote(changeNoteCommand);

        em.flush();
        em.clear();
        ReadingLog foundReadingLog = readingLogRepository.findByIdAndEmail(readingLogId, email)
            .orElseThrow(() -> new AssertionError("ReadingLog not found"));

        assertThat(foundReadingLog.getNote()).isEqualTo(note);
    }

    @Test
    void changeRating_updateRatingInDB() {
        int rating = 5;

        readingLogApplicationService.changeRating(rating, readingLogId);

        em.flush();
        em.clear();

        ReadingLog foundReadingLog = readingLogRepository.findByIdAndEmail(readingLogId, email)
            .orElseThrow(() -> new AssertionError("ReadingLog not found"));

        assertThat(foundReadingLog.getRating()).isEqualTo(rating);
    }
}
