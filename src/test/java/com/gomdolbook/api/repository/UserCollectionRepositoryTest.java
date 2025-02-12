package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.config.QueryDslConfig;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Import(QueryDslConfig.class)
@DataJpaTest
class UserCollectionRepositoryTest {

    @Autowired
    UserCollectionRepository userCollectionRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookUserCollectionRepository bookUserCollectionRepository;

    @BeforeEach
    void setUp() {
        User user = createUser("user@gmail.com", "image");
        ReadingLog savedReadingLog = createReadingLog(user);
        Book mockBook = createBook(savedReadingLog);
        UserCollection collection = createUserCollection("컬렉션", user);
        UserCollection collection1 = createUserCollection("컬렉션1", user);
        createBookUserCollection(mockBook, collection, user);
        createBookUserCollection(mockBook, collection1, user);
    }

    @AfterEach
    void tearDown() {
        bookUserCollectionRepository.deleteAll();
        bookRepository.deleteAll();
        userCollectionRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByEmail() {
        List<BookUserCollection> byEmail = bookUserCollectionRepository.findByUserEmail(
            "user@gmail.com");

        assertThat(byEmail).size().isEqualTo(2);
        assertThat(byEmail.getLast().getUserCollection().getName()).isEqualTo("컬렉션1");
    }

    private Book createBook(ReadingLog readingLog) {
        Book book =  Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
        book.setReadingLog(readingLog);
        return bookRepository.save(book);
    }

    private ReadingLog createReadingLog(User user) {
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLog.setUser(user);
        return readingLogRepository.save(readingLog);
    }

    private User createUser(String email, String pic) {
        User user = new User(email, pic, Role.USER);
        return userRepository.save(user);
    }

    private UserCollection createUserCollection(String name, User user) {
        UserCollection collection = new UserCollection(name);
        collection.setUser(user);
        return userCollectionRepository.save(collection);
    }

    private BookUserCollection createBookUserCollection(Book book, UserCollection userCollection,
        User user) {
        BookUserCollection bookUserCollection = new BookUserCollection();
        bookUserCollection.setBook(book);
        bookUserCollection.setUserCollection(userCollection);
        bookUserCollection.setUser(user);
        return bookUserCollectionRepository.save(bookUserCollection);
    }

}
