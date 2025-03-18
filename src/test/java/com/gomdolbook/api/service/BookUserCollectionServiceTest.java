package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.api.dto.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
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

@Slf4j
@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookUserCollectionServiceTest {

    static UserCollection collection;
    static Book mockBook;
    static User user;

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
        bookUserCollectionRepository.deleteAll();
        bookRepository.deleteAll();
        userCollectionRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getList() {
        var collectionList = bookUserCollectionService.getCollectionList();
        assertThat(collectionList).hasSize(1);
        assertThat(collectionList.getLast().getName()).isEqualTo("컬렉션");
        assertThat(collectionList.getLast().getBooks().getCovers()).hasSize(1);
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
        assertThat(collectionList.getLast().getName()).isEqualTo("test");
    }

    @Test
    void addBookWithStatus() {
        BookSaveRequestDTO requestDTO = getBookSaveRequest();

        bookUserCollectionService.addBook(requestDTO, "컬렉션");

        var collectionList = bookUserCollectionService.getCollectionList();
        assertThat(collectionList).hasSize(1);
        assertThat(collectionList.getLast().getName()).isEqualTo("컬렉션");
        assertThat(collectionList.getLast().getBooks().getCovers()).hasSize(2);
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

        assertThat(list.getFirst().getBooks().getCovers()).hasSize(2);
    }

    @Test
    void addBookWithNullStatus() {
        BookSaveRequestDTO requestDTO = BookSaveRequestDTO.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .status(null)
            .build();

        bookUserCollectionService.addBook(requestDTO, "컬렉션");
        String status = bookService.getStatus("9788936434120");

        assertThat(status).isEqualTo("NEW");
    }

    @Test
    void addBookWithBlankStatus() {
        BookSaveRequestDTO requestDTO = BookSaveRequestDTO.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("노벨 문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .status("")
            .build();

        bookUserCollectionService.addBook(requestDTO, "컬렉션");
        String status = bookService.getStatus("9788936434120");

        assertThat(status).isEqualTo("NEW");
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
    void getCollection2() {
        BookSaveRequestDTO requestDTO = getBookSaveRequest();
        bookUserCollectionService.addBook(requestDTO, "컬렉션");

        List<BookListResponseDTO> c = bookUserCollectionService.getCollection("컬렉션");

        assertThat(c).hasSize(2);
        assertThat(c.getLast().title()).isEqualTo("소년이 온다");
        assertThat(c.getLast().cover()).isEqualTo("image 한강");
    }

    @Test
    void testDuplicatedBookSave() {
        BookSaveRequestDTO requestDTO = getBookSaveRequest();
        bookUserCollectionService.addBook(requestDTO, "컬렉션");

        BookSaveRequestDTO requestDTO2 = BookSaveRequestDTO.builder()
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

}
