package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.BookSaveRequestDTO;
import com.gomdolbook.api.common.config.QueryDslConfig;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.readingLog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.util.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import({QueryDslConfig.class, TestDataFactory.class})
@DataJpaTest
class BookRepositoryTest {

    static Book mockBook;
    static User user;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("user@gmail.com", "image");
        ReadingLog savedReadingLog = testDataFactory.createReadingLog(user);
        mockBook = testDataFactory.createBook(savedReadingLog);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveBook() {
        Book saved = bookRepository.find("9788991290402").orElseThrow();

        assertThat(saved.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(saved.getReadingLog().getNote1()).isEqualTo("");
    }

    @Transactional
    @Test
    void updateStatus() {
        BookSaveRequestDTO request = testDataFactory.getBookSaveRequestDTO("FINISHED");
        Book book = bookRepository.find(request.isbn13()).orElseThrow();

        if (!book.getReadingLog().getStatus().name().equals(request.status())) {
            book.getReadingLog().changeStatus(Status.FINISHED);
        }

        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("FINISHED");
    }

    @Transactional
    @Test
    void getReadingLog() {
        BookAndReadingLogData savedBook = bookRepository.find(
            "user@gmail.com", "9788991290402").orElseThrow();

        assertThat(savedBook.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(savedBook.getStatus().name()).isEqualTo("READING");
    }

    @Test
    void getLibrary() {
        List<BookListData> dto = bookRepository.find(
            Status.READING, "user@gmail.com");
        assertThat(dto).hasSize(1);
        assertThat(dto.getFirst().title()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getBookResponse() {
        List<BookListData> byReadingStatus = bookRepository.find(
            Status.READING, "user@gmail.com");

        assertThat(byReadingStatus).hasSize(1);
    }

    @Test
    void getStatus() {
        Status status = bookRepository.getStatus("9788991290402", "user@gmail.com").orElseThrow();

        assertThat(status.name()).isEqualTo("READING");
    }
}
