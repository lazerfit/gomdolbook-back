package com.gomdolbook.api.repository.reactive;

import com.gomdolbook.api.models.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@AutoConfigureDataR2dbc
class BookReactiveRepositoryTest {

    @Autowired
    BookReactiveRepository bookReactiveRepository;

    @BeforeEach
    void setUp() {
        Book book = Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();

        bookReactiveRepository.save(book).block();
    }

    @AfterEach
    void tearDown() {
        bookReactiveRepository.deleteAll().block();
    }

    @Test
    void findBook() {
        // Arrange
        String isbn13 = "9788991290402";

        // Act && Assert
        StepVerifier.create(bookReactiveRepository.findByIsbn13(isbn13))
            .expectNextMatches(b -> b.getId() != null && b.getTitle().equals("펠로폰네소스 전쟁사"))
            .verifyComplete();
    }

    @Test
    void findBookOrSaveAndReturnIfNotFound() {
        //Arrange
        Book book = Book.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("2024 노벨문학상")
            .isbn13("9788936434120")
            .cover("image1")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();

        //Act && Assert
        StepVerifier.create(findBookSwitchIfEmpty(book))
            .expectNextMatches(b -> b.getTitle().equals("소년이 온다"))
            .verifyComplete();
    }

    private Mono<Book> findBookSwitchIfEmpty(Book book) {
        return bookReactiveRepository.findByIsbn13(book.getIsbn13())
            .switchIfEmpty(bookReactiveRepository.save(book));
    }
}
