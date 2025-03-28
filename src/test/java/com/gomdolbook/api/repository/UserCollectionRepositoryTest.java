package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.api.dto.book.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.config.QueryDslConfig;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import com.gomdolbook.api.util.TestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({QueryDslConfig.class, TestDataFactory.class})
@DataJpaTest
class UserCollectionRepositoryTest {

    static UserCollection collection;
    static Book mockBook;
    static User user;

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

    @Autowired
    TestDataFactory testDataFactory;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("user@gmail.com", "image");
        ReadingLog savedReadingLog = testDataFactory.createReadingLog(user);
        mockBook = testDataFactory.createBook(savedReadingLog);
        collection = testDataFactory.createUserCollection("컬렉션", user);
        testDataFactory.createBookUserCollection(mockBook, collection, user);

        User testUser = testDataFactory.createUser("test@email.com", "image");
        testDataFactory.createUserCollection("test", testUser);
    }

    @AfterEach
    void tearDown() {
        em.clear();
        bookUserCollectionRepository.deleteAll();
        bookRepository.deleteAll();
        readingLogRepository.deleteAll();
        userCollectionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByEmail() {
        User user1 = userRepository.findByEmail("user@gmail.com").orElseThrow();
        List<BookUserCollection> byEmail = bookUserCollectionRepository.findByUser(
            user1);

        assertThat(byEmail).size().isEqualTo(1);
        assertThat(byEmail.getLast().getUserCollection().getName()).isEqualTo("컬렉션");
    }

    @Test
    void getCollection() {
        List<BookListResponseDTO> result = bookUserCollectionRepository.getCollection("컬렉션",
            "user@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status().name()).isEqualTo("READING");
    }

    @Test
    void getEmptyCollection() {
        List<BookListResponseDTO> result = bookUserCollectionRepository.getCollection("test",
            "user@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void getTwoCollections() {
        testDataFactory.createUserCollection("col1", user);

        List<BookCollectionCoverDTO> allCollection = bookUserCollectionRepository.getAllCollection(
            "user@gmail.com");

        assertThat(allCollection).hasSize(2);
        assertThat(allCollection.getFirst().name()).isEqualTo("컬렉션");
        assertThat(allCollection.getLast().name()).isEqualTo("col1");
    }

    @Test
    void getOneBookCover() {
        List<BookCollectionCoverDTO> covers = bookUserCollectionRepository.getAllCollection(
            "user@gmail.com");

        assertThat(covers).hasSize(1);
    }

    @Test
    void getMultipleBookCovers() {
        Book book =  Book.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();
        Book saved = bookRepository.save(book);
        testDataFactory.createBookUserCollection(saved, collection, user);

        List<BookCollectionCoverDTO> collectionDTO = bookUserCollectionRepository.getAllCollection(
            "user@gmail.com");

        assertThat(collectionDTO).hasSize(2);
        assertThat(collectionDTO.getFirst().name()).isEqualTo("컬렉션");
        assertThat(collectionDTO.getLast().name()).isEqualTo("컬렉션");
        assertThat(collectionDTO.getFirst().cover()).isEqualTo("image");
        assertThat(collectionDTO.getLast().cover()).isEqualTo("image 한강");
    }

    @Test
    void findSpecificBook() {
        Book book =  Book.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();
        Book saved = bookRepository.save(book);
        testDataFactory.createBookUserCollection(saved, collection, user);

        Optional<BookUserCollection> result = bookUserCollectionRepository.findByIsbnAndName(
            "9788991290402", "컬렉션", "user@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getBook().getTitle()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getOne() {
        User user1 = userRepository.findByEmail("user@gmail.com").orElseThrow();
        UserCollection userCollection = userCollectionRepository.findByName("컬렉션", user1.getEmail())
            .orElseThrow();
        List<BookUserCollection> c = bookUserCollectionRepository.findByUserAndUserCollection(
            user1, userCollection);

        assertThat(c).hasSize(1);
    }

}
