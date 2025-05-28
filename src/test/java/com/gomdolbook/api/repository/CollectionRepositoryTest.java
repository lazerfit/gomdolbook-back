package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.common.config.QueryDslConfig;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookcollection.BookCollection;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.bookcollection.BookCollectionRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.UserRepository;
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
class CollectionRepositoryTest {

    static Collection collection;
    static Book mockBook;
    static User user;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookCollectionRepository bookCollectionRepository;

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
        bookCollectionRepository.deleteAll();
        bookRepository.deleteAll();
        readingLogRepository.deleteAll();
        collectionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByEmail() {
        User user1 = userRepository.find("user@gmail.com").orElseThrow();
        List<BookCollection> byEmail = bookCollectionRepository.find(
            user1);

        assertThat(byEmail).size().isEqualTo(1);
        assertThat(byEmail.getLast().getCollection().getName()).isEqualTo("컬렉션");
    }

    @Test
    void getCollection() {
        List<BookListData> result = bookCollectionRepository.getCollection("컬렉션",
            "user@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status().name()).isEqualTo("READING");
    }

    @Test
    void getEmptyCollection() {
        List<BookListData> result = bookCollectionRepository.getCollection("test",
            "user@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void getTwoCollections() {
        testDataFactory.createUserCollection("col1", user);

        List<BookCollectionCoverData> allCollection = bookCollectionRepository.getAllCollection(
            "user@gmail.com");

        assertThat(allCollection).hasSize(2);
        assertThat(allCollection.getFirst().name()).isEqualTo("컬렉션");
        assertThat(allCollection.getLast().name()).isEqualTo("col1");
    }

    @Test
    void getOneBookCover() {
        List<BookCollectionCoverData> covers = bookCollectionRepository.getAllCollection(
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
            .isbn("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();
        Book saved = bookRepository.save(book);
        testDataFactory.createBookUserCollection(saved, collection, user);

        List<BookCollectionCoverData> collectionDTO = bookCollectionRepository.getAllCollection(
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
            .isbn("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .build();
        Book saved = bookRepository.save(book);
        testDataFactory.createBookUserCollection(saved, collection, user);

        Optional<BookCollection> result = bookCollectionRepository.find(
            "9788991290402", "컬렉션", "user@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getBook().getTitle()).isEqualTo("펠로폰네소스 전쟁사");
    }

    @Test
    void getOne() {
        User user1 = userRepository.find("user@gmail.com").orElseThrow();
        Collection collection = collectionRepository.find("컬렉션", user1.getEmail())
            .orElseThrow();
        List<BookCollection> c = bookCollectionRepository.find(
            user1, collection);

        assertThat(c).hasSize(1);
    }

}
