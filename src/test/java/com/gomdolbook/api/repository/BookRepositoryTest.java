package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    private Book getMockBook() {
        return Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
    }

    @Test
    void saveBook() {
        User user = new User("user", "img", Role.USER);
        ReadingLog readingLog = readingLogRepository.save(
            new ReadingLog(Status.READING, "1", "2", "3"));
        Book book = getMockBook();
        book.addReadingLog(readingLog);
        Book saved = bookRepository.save(book);

        assertThat(saved.getAuthor()).isEqualTo("투퀴디데스");
        assertThat(saved.getReadingLog().getNote1()).isEqualTo("1");
    }

    @Test
    void findBookByIsbn() {
        Book book = getMockBook();
        bookRepository.save(book);
        Book book1 = bookRepository.findByIsbn13("9788991290402").orElseThrow();

        assertThat(book1.getAuthor()).isEqualTo("투퀴디데스");
    }
}
