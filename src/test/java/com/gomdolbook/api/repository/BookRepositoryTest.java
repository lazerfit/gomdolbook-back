package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.config.QueryDslConfig;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
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
        Book saved = bookRepository.findByIsbn13("9788991290402").orElseThrow();

        assertThat(saved.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(saved.getReadingLog().getNote1()).isEqualTo("1");
    }

    @Transactional
    @Test
    void getReadingLog() {
        BookAndReadingLogDTO savedBook = bookRepository.findByUserEmailAndIsbn(
            "user@gmail.com", "9788991290402").orElseThrow();

        assertThat(savedBook.getAuthor()).isEqualTo("투퀴디데스");
    }

    @Test
    void getLibrary() {
        List<BookListResponseDTO> dto = bookRepository.findByReadingStatus(
            Status.READING, "user@gmail.com");
        assertThat(dto).hasSize(1);
        assertThat(dto.getFirst().title()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getBookResponse() {
        List<BookListResponseDTO> byReadingStatus = bookRepository.findByReadingStatus(
            Status.READING, "user@gmail.com");

        assertThat(byReadingStatus).hasSize(1);
        assertThat(byReadingStatus.getFirst().isReadingLogExists()).isTrue();
    }
}
