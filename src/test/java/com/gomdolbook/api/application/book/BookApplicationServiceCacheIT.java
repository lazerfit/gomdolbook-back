package com.gomdolbook.api.application.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@Transactional
@SpringBootTest
class BookApplicationServiceCacheIT {

    @Autowired
    BookApplicationService bookApplicationService;

    @MockitoBean
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookMetaRepository bookMetaRepository;

    @Autowired
    private CacheManager cm;

    private final String email = "test@email.com";

    @BeforeEach
    void setUp() {
        User user = new User(email, "pic", Role.USER);
        userRepository.save(user);
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        bookMetaRepository.save(BookMeta.of(bookSaveCommand));
    }

    @AfterEach
    void cleanUp() {
        cm.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cm.getCache(cacheName)).clear());
    }

    @Test
    void getLibrary() {
        when(bookRepository.findLibraryByStatus(Status.READING, email))
            .thenReturn(List.of(new BookListData("c", "t", "i", Status.READING, 1L)));

        List<BookListData> library1 = bookApplicationService.getLibrary("READING");
        List<BookListData> library2 = bookApplicationService.getLibrary("READING");

        assertThat(library1).isSameAs(library2);
        verify(bookRepository, times(1)).findLibraryByStatus(Status.READING, email);
    }

    @Test
    void getFinishedBookCalendarData() {
        when(bookRepository.findFinishedBookCalendarData(email))
            .thenReturn(List.of(new FinishedBookCalendarData("t","i","c",5,
                LocalDate.of(2025,1,1).atStartOfDay())));

        List<FinishedBookCalendarData> result1 = bookApplicationService.getFinishedBookCalendarData();
        List<FinishedBookCalendarData> result2 = bookApplicationService.getFinishedBookCalendarData();

        assertThat(result1).isSameAs(result2);
        verify(bookRepository, times(1)).findFinishedBookCalendarData(email);
    }

    @Test
    void changeStatus_FINISHEDStatus_ShouldEvictAll() {
        when(bookRepository.findFinishedBookCalendarData(email))
            .thenReturn(List.of(new FinishedBookCalendarData("t","i","c",5,
                LocalDate.of(2025,1,1).atStartOfDay())));
        when(bookRepository.findLibraryByStatus(Status.READING, email))
            .thenReturn(List.of(new BookListData("c", "t", "i", Status.READING, 1L)));
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        when(bookRepository.findByIsbn("i", email))
            .thenReturn(Optional.of(
                Book.of(BookMeta.of(bookSaveCommand), new User(email, "pic", Role.USER))));
        when(bookRepository.findStatus("i", email)).thenReturn(Optional.of(Status.READING));

        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");
        bookApplicationService.getStatus("i");

        bookApplicationService.changeStatus("i", "FINISHED");
        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");
        bookApplicationService.getStatus("i");

        verify(bookRepository, times(2)).findFinishedBookCalendarData(email);
        verify(bookRepository, times(2)).findLibraryByStatus(Status.READING, email);
        verify(bookRepository, times(2)).findStatus("i", email);
    }

    @Test
    void changeStatus_TOREADStatus_ShouldEvictLibraryCacheOnly() {
        when(bookRepository.findFinishedBookCalendarData(email))
            .thenReturn(List.of(new FinishedBookCalendarData("t","i","c",5,
                LocalDate.of(2025,1,1).atStartOfDay())));
        when(bookRepository.findLibraryByStatus(Status.READING, email))
            .thenReturn(List.of(new BookListData("c", "t", "i", Status.READING, 1L)));
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        when(bookRepository.findByIsbn("i", email))
            .thenReturn(Optional.of(
                Book.of(BookMeta.of(bookSaveCommand), new User(email, "pic", Role.USER))));
        when(bookRepository.findStatus("i", email)).thenReturn(Optional.of(Status.READING));

        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");
        bookApplicationService.getStatus("i");

        bookApplicationService.changeStatus("i", "TO_READ");
        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");
        bookApplicationService.getStatus("i");

        verify(bookRepository, times(1)).findFinishedBookCalendarData(email);
        verify(bookRepository, times(2)).findLibraryByStatus(Status.READING, email);
        verify(bookRepository, times(2)).findStatus("i", email);
    }

    @Test
    void getStatus() {
        when(bookRepository.findStatus("i", email)).thenReturn(Optional.of(Status.READING));

        bookApplicationService.getStatus("i");
        bookApplicationService.getStatus("i");

        verify(bookRepository, times(1)).findStatus("i", email);
    }

    @Test
    void addBookToLibrary() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        when(bookRepository.findFinishedBookCalendarData(email))
            .thenReturn(List.of(new FinishedBookCalendarData("t","i","c",5,
                LocalDate.of(2025,1,1).atStartOfDay())));
        when(bookRepository.findLibraryByStatus(Status.READING, email))
            .thenReturn(List.of(new BookListData("c", "t", "i", Status.READING, 1L)));
        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");

        bookApplicationService.addBookToLibrary(bookSaveCommand);
        bookApplicationService.getFinishedBookCalendarData();
        bookApplicationService.getLibrary("READING");

        verify(bookRepository, times(2)).findFinishedBookCalendarData(email);
        verify(bookRepository, times(2)).findLibraryByStatus(Status.READING, email);
    }
}
