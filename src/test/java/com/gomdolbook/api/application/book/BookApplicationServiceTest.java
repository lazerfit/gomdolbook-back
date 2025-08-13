package com.gomdolbook.api.application.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.user.UserApplicationService;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookApplicationServiceTest {

    @InjectMocks
    private BookApplicationService bookApplicationService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private BookMetaRepository bookMetaRepository;

    @Mock
    private SecurityService securityService;

    private final String email = "test@email.com";

    @Test
    void addBookToLibrary() {
        BookMeta bookMeta = BookMeta.builder().title("t").author("a").pubDate("p")
            .description("d").isbn("i").cover("c").categoryName("ca").publisher("p").build();
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        when(userApplicationService.find(email)).thenReturn(Optional.of(new User(email, "pic", Role.USER)));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);

        bookApplicationService.addBookToLibrary(bookSaveCommand);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void getLibrary() {
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(bookRepository.findLibraryByStatus(Status.READING, email))
            .thenReturn(List.of(new BookListData("c", "t", "i", Status.READING, 1L)));

        bookApplicationService.getLibrary("READING");

        verify(bookRepository,times(1)).findLibraryByStatus(Status.READING, email);
    }

    @Test
    void getFinishedBookCalendarData() {
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(bookRepository.findFinishedBookCalendarData(email))
            .thenReturn(List.of(new FinishedBookCalendarData("t","i","c",5,
                LocalDate.of(2025,1,1).atStartOfDay())));

        bookApplicationService.getFinishedBookCalendarData();

        verify(bookRepository, times(1)).findFinishedBookCalendarData(email);
    }

    @Test
    void changeStatus() {
        BookSaveCommand bookSaveCommand = new BookSaveCommand("t", "a", "p", "d", "i", "c", "ca",
            "p", "READING");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(bookRepository.findByIsbn("i", email))
            .thenReturn(Optional.of(
                Book.of(BookMeta.of(bookSaveCommand), new User(email, "pic", Role.USER))));

        bookApplicationService.changeStatus("i", "TO_READ");

        verify(bookRepository, times(1)).findByIsbn("i", email);
    }
}
