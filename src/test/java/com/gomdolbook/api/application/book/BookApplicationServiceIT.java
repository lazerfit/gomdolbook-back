package com.gomdolbook.api.application.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@WithMockCustomUser
@Transactional
@SpringBootTest
class BookApplicationServiceIT {

    @Autowired
    private BookApplicationService bookApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookMetaRepository bookMetaRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        User user = new User("test@email.com", "pic", Role.USER);
        userRepository.save(user);
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        bookMetaRepository.save(BookMeta.of(bookSaveCommand));
    }

    @Test
    void addBookToLibrary() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        bookApplicationService.addBookToLibrary(bookSaveCommand);

        em.flush();
        em.clear();

        List<BookListData> reading = bookApplicationService.getLibrary("READING");
        assertThat(reading).hasSize(1);
    }

    @Test
    void addBookToLibrary_IllegalArgumentException() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "ILLEGAL");

        assertThatThrownBy(() -> bookApplicationService.addBookToLibrary(bookSaveCommand))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getLibrary_Empty() {
        List<BookListData> finished = bookApplicationService.getLibrary("FINISHED");

        assertThat(finished).isEmpty();
    }

    @Test
    void getFinishedBookCalendarData() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "FINISHED");
        bookApplicationService.addBookToLibrary(bookSaveCommand);
        em.flush();
        em.clear();

        List<FinishedBookCalendarData> result = bookApplicationService.getFinishedBookCalendarData();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getIsbn()).isEqualTo("i");
    }

    @Test
    void changeStatus() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "FINISHED");
        bookApplicationService.addBookToLibrary(bookSaveCommand);
        em.flush();
        em.clear();

        bookApplicationService.changeStatus("i", "READING");
        em.flush();
        em.clear();

        StatusData status = bookApplicationService.getStatus("i");
        assertThat(status.status()).isEqualTo("READING");
    }

    @Test
    void changeStatus_InvalidIsbn() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "FINISHED");
        bookApplicationService.addBookToLibrary(bookSaveCommand);
        em.flush();
        em.clear();

        assertThatThrownBy(() -> bookApplicationService.changeStatus("invalid", "READING"))
            .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void getStatus() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "FINISHED");
        bookApplicationService.addBookToLibrary(bookSaveCommand);
        em.flush();
        em.clear();

        StatusData status = bookApplicationService.getStatus("i");
        assertThat(status.status()).isEqualTo("FINISHED");
    }

    @Test
    void getStatus_invalid_shouldReturn_EMPTY() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "FINISHED");
        bookApplicationService.addBookToLibrary(bookSaveCommand);
        em.flush();
        em.clear();

        StatusData status = bookApplicationService.getStatus("iiii");
        assertThat(status.status()).isEqualTo("EMPTY");
    }
}
