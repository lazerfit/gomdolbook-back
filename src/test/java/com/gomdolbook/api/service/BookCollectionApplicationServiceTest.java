package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverListData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookcollection.BookCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
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
class BookCollectionApplicationServiceTest {

    static Collection collection;
    static Book mockBook;
    static User user;

    @PersistenceContext
    EntityManager em;

    @Autowired
    BookApplicationService bookApplicationService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookCollectionApplicationService bookCollectionApplicationService;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    BookCollectionRepository bookCollectionRepository;

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
        bookCollectionRepository.deleteAllInBatch();
        collectionRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        readingLogRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void getList() {
        var collectionList = bookCollectionApplicationService.getCollectionList();
        assertThat(collectionList).hasSize(1);
        assertThat(collectionList.getLast().name()).isEqualTo("컬렉션");
        assertThat(collectionList.getLast().books().getCovers()).hasSize(1);
    }

    @WithMockCustomUser(email = "test12@email.com")
    @Test
    void getEmptyList() {
        var collectionList = bookCollectionApplicationService.getCollectionList();
        assertThat(collectionList).isEmpty();
    }

    @WithMockCustomUser(email = "test@email.com")
    @Test
    void getOnlyCollectionName() {
        List<BookCollectionCoverListData> collectionList = bookCollectionApplicationService.getCollectionList();
        assertThat(collectionList.getLast().name()).isEqualTo("한강");
    }

    @Test
    void addBookCacheTest() {
        bookCollectionApplicationService.getCollectionList();
        bookCollectionApplicationService.getCollectionList();
        bookCollectionApplicationService.getCollection("컬렉션");
        bookCollectionApplicationService.getCollection("컬렉션");
        Cache cache = cacheManager.getCache("collectionListCache");
        assertThat(cache.get("redkafe@daum.net")).isNotNull();
        BookSaveCommand requestDTO = getBookSaveRequest();
        bookCollectionApplicationService.addBook(requestDTO, "컬렉션");
        bookCollectionApplicationService.getCollectionList();
        List<BookCollectionCoverListData> list = cache.get("redkafe@daum.net", List.class);

        assertThat(list.getFirst().books().getCovers()).hasSize(2);
    }

    @WithMockCustomUser(email = "test@email.com")
    @Test
    void createCollection() {
        bookCollectionApplicationService.createCollection("한강");
        var collectionList = bookCollectionApplicationService.getCollectionList();

        assertThat(collectionList).hasSize(2);
    }

    @Test
    void getCollection() {
        List<BookListData> c = bookCollectionApplicationService.getCollection("컬렉션");

        assertThat(c).hasSize(1);
        assertThat(c.getLast().title()).isEqualTo("펠로폰네소스 전쟁사");
        assertThat(c.getLast().cover()).isEqualTo("image");
    }

    @Test
    void testDuplicatedBookSave() {
        BookSaveCommand requestDTO = getBookSaveRequest();
        bookCollectionApplicationService.addBook(requestDTO, "컬렉션");

        BookSaveCommand requestDTO2 = getBookSaveRequest();
        bookCollectionApplicationService.addBook(requestDTO2, "컬렉션");

        List<BookListData> collection1 = bookCollectionApplicationService.getCollection("컬렉션");

        assertThat(collection1).hasSize(2);
    }

    @Test
    void removeBook() {
        bookCollectionApplicationService.removeBook("9788991290402", "컬렉션");

        List<BookListData> c = bookCollectionApplicationService.getCollection("컬렉션");

        assertThat(c).isEmpty();
    }

    private BookSaveCommand getBookSaveRequest() {
        return new BookSaveCommand(
            "소년이 온다", "한강", "2014-05-19", "노벨 문학상", "9788936434120", "image 한강", "노벨문학상", "창비",
            "READING"
        );
    }

    @Transactional
    @Test
    void removeBookUserCollection() {
        Collection uc = testDataFactory.createUserCollection("c", user);
        testDataFactory.createBookUserCollection(mockBook, uc, user);
        List<BookListData> list = bookCollectionApplicationService.getCollection("c");
        assertThat(list).hasSize(1);

        bookCollectionApplicationService.deleteCollection("c");
    }

}
