package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.common.config.QueryDslConfig;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import({QueryDslConfig.class, TestDataFactory.class})
@DataJpaTest
class BookRepositoryCustomTest {

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
        ReadingLog readingLog = testDataFactory.createReadingLog(user);
        mockBook = testDataFactory.createBook(readingLog);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveBook() {
        Book saved = bookRepository.findByIsbn("9788991290402").orElseThrow();

        assertThat(saved.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(saved.getReadingLog().getNote1()).isEmpty();
    }

    @Transactional
    @Test
    void updateStatus() {
        BookSaveCommand request = testDataFactory.getBookSaveRequestDTO("FINISHED");
        Book book = bookRepository.findByIsbn(request.isbn()).orElseThrow();

        if (!book.getReadingLog().getStatus().name().equals(request.status())) {
            book.getReadingLog().changeStatus(Status.FINISHED);
        }

        assertThat(book.getReadingLog().getStatus().name()).isEqualTo("FINISHED");
    }

    @Test
    void getStatus() {
        Status status = bookRepository.getStatus("9788991290402", "user@gmail.com").orElseThrow();

        assertThat(status.name()).isEqualTo("READING");
    }
}
