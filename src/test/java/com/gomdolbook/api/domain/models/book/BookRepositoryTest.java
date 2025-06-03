package com.gomdolbook.api.domain.models.book;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;
    @Autowired
    BookMetaRepository bookMetaRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReadingLogRepository readingLogRepository;

    @BeforeEach
    void setUp() {
        BookSaveCommand command = new BookSaveCommand(
            "제목", "저자", "2025-01-01", "설명",
            "isbn", "cover", "카테고리", "출판사", "READING"
        );
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        User user = new User("test@email.com", "img", Role.USER);
        ReadingLog readingLog = readingLogRepository.save(ReadingLog.of(user, Status.READING));
        Book book = Book.of(bookMeta);
        book.setReadingLog(readingLog);
        bookRepository.save(book);
        userRepository.save(user);
    }

    @Test
    void findByIsbn_정상작동() {
        Book book = bookRepository.findByIsbn("isbn").orElseThrow();
        assertThat(book.getBookMeta().getIsbn()).isEqualTo("isbn");
    }

    @Test
    void findByIsbn_존재하지않으면_빈Optional() {
        Optional<Book> notFound = bookRepository.findByIsbn("not-exist");
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByEmail_정상조회() {
        Optional<BookAndReadingLogData> found = bookRepository.findByEmail("test@email.com", "isbn");
        assertThat(found).isPresent();
        assertThat(found.get().getAuthor()).isEqualTo("저자");
    }

    @Test
    void findByEmail_없는유저_빈Optional() {
        Optional<BookAndReadingLogData> found = bookRepository.findByEmail("no@user.com", "isbn");
        assertThat(found).isEmpty();
    }

    @Test
    void findByStatus_정상조회() {
        List<BookListData> list = bookRepository.findByStatus(Status.READING, "test@email.com");
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().isbn()).isEqualTo("isbn");
    }

    @Test
    void findByStatus_없는상태_빈리스트() {
        List<BookListData> list = bookRepository.findByStatus(Status.FINISHED, "test@email.com");
        assertThat(list).isEmpty();
    }

    @Test
    void getStatus_없는책_빈Optional() {
        Optional<Status> status = bookRepository.getStatus("no-isbn", "test@email.com");
        assertThat(status).isEmpty();
    }

    @Test
    void getStatus_정상조회() {
        Optional<Status> status = bookRepository.getStatus("isbn", "test@email.com");
        assertThat(status).contains(Status.READING);
    }

    @Test
    void getFinishedCalendarData_완독책없음_빈리스트() {
        List<FinishedBookCalendarData> list = bookRepository.getFinishedBookCalendarData(
            "test@email.com");
        assertThat(list).isEmpty();
    }

    @Test
    void getFinishedBookCalendarData_완독책있음_정상조회() {
        // 상태를 FINISHED로 변경
        ReadingLog readingLog = readingLogRepository.findByIsbnAndEmail("isbn", "test@email.com")
            .orElseThrow();
        readingLog.changeStatus(Status.FINISHED);
        readingLogRepository.save(readingLog);

        List<FinishedBookCalendarData> list = bookRepository.getFinishedBookCalendarData("test@email.com");
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getIsbn()).isEqualTo("isbn");
    }
}
