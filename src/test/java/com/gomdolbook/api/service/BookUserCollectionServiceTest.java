package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.api.dto.book.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookSaveRequestDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.persistence.entity.Book;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookUserCollectionServiceTest {

    static UserCollection collection;
    static Book mockBook;
    static User user;

    @PersistenceContext
    EntityManager em;

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookUserCollectionService bookUserCollectionService;

    @Autowired
    UserCollectionRepository userCollectionRepository;

    @Autowired
    BookUserCollectionRepository bookUserCollectionRepository;

    @Autowired
    TestDataFactory testDataFactory;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("redkafe@daum.net", "image");
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
        bookUserCollectionRepository.deleteAllInBatch();
        userCollectionRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        readingLogRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void getList() {
        var collectionList = bookUserCollectionService.getCollectionList();
        assertThat(collectionList).hasSize(1);
        assertThat(collectionList.getLast().name()).isEqualTo("컬렉션");
        assertThat(collectionList.getLast().books().getCovers()).hasSize(1);
    }

    @WithMockCustomUser(email = "test12@email.com")
    @Test
    void getEmptyList() {
        var collectionList = bookUserCollectionService.getCollectionList();
        assertThat(collectionList).isEmpty();
    }

    @WithMockCustomUser(email = "test@email.com")
    @Test
    void getOnlyCollectionName() {
        List<BookCollectionCoverListResponseDTO> collectionList = bookUserCollectionService.getCollectionList();
        assertThat(collectionList.getLast().name()).isEqualTo("한강");
    }

    @Test
    void addBookCacheTest() {
        bookUserCollectionService.getCollectionList();
        bookUserCollectionService.getCollectionList();
        bookUserCollectionService.getCollection("컬렉션");
        bookUserCollectionService.getCollection("컬렉션");
        Cache cache = cacheManager.getCache("collectionListCache");
        assertThat(cache.get("redkafe@daum.net")).isNotNull();
        BookSaveRequestDTO requestDTO = getBookSaveRequest();

        bookUserCollectionService.addBook(requestDTO, "컬렉션");
        bookUserCollectionService.getCollectionList();
        List<BookCollectionCoverListResponseDTO> list = cache.get("redkafe@daum.net", List.class);

        assertThat(list.getFirst().books().getCovers()).hasSize(2);
    }

    @WithMockCustomUser(email = "test@email.com")
    @Test
    void createCollection() {
        bookUserCollectionService.createCollection("한강");
        var collectionList = bookUserCollectionService.getCollectionList();

        assertThat(collectionList).hasSize(2);
    }

    @Test
    void getCollection() {
        List<BookListResponseDTO> c = bookUserCollectionService.getCollection("컬렉션");

        assertThat(c).hasSize(1);
        assertThat(c.getLast().title()).isEqualTo("펠로폰네소스 전쟁사");
        assertThat(c.getLast().cover()).isEqualTo("image");
    }

    @Test
    void testDuplicatedBookSave() {
        BookSaveRequestDTO requestDTO = getBookSaveRequest();
        bookUserCollectionService.addBook(requestDTO, "컬렉션");

        BookSaveRequestDTO requestDTO2 = getBookSaveRequest();
        bookUserCollectionService.addBook(requestDTO2, "컬렉션");

        List<BookListResponseDTO> collection1 = bookUserCollectionService.getCollection("컬렉션");

        assertThat(collection1).hasSize(2);
    }

    @Test
    void deleteBook() {
        bookUserCollectionService.deleteBook("9788991290402", "컬렉션");

        List<BookListResponseDTO> c = bookUserCollectionService.getCollection("컬렉션");

        assertThat(c).isEmpty();
    }

    private BookSaveRequestDTO getBookSaveRequest() {
        return BookSaveRequestDTO.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .status("READING")
            .build();
    }

    @Transactional
    @Test
    void deleteBookUserCollection() {
        UserCollection uc = testDataFactory.createUserCollection("c", user);
        testDataFactory.createBookUserCollection(mockBook, uc, user);
        List<BookListResponseDTO> list = bookUserCollectionService.getCollection("c");
        assertThat(list).hasSize(1);

        bookUserCollectionService.deleteCollection("c");
    }

}
