package com.gomdolbook.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserCollectionServiceTest {

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserCollectionService userCollectionService;

    @Autowired
    UserCollectionRepository userCollectionRepository;

    @Autowired
    BookUserCollectionRepository bookUserCollectionRepository;

    @BeforeEach
    void setUp() {
        User user = new User("redkafe@daum.net", "img", Role.USER);
        userRepository.save(user);
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLog.setUser(user);
        ReadingLog savedReadingLog = readingLogRepository.save(readingLog);
        Book mockBook = Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
        mockBook.setReadingLog(savedReadingLog);
        UserCollection collection = new UserCollection("컬렉션");
        UserCollection collection1 = new UserCollection("컬렉션1");
        collection.setUser(user);
        collection1.setUser(user);
        userCollectionRepository.save(collection);
        userCollectionRepository.save(collection1);
        BookUserCollection bookUserCollection = new BookUserCollection();
        bookUserCollection.setBook(mockBook);
        bookUserCollection.setUserCollection(collection);
        bookUserCollection.setUser(user);
        bookUserCollectionRepository.save(bookUserCollection);
        BookUserCollection bookUserCollection1 = new BookUserCollection();
        bookUserCollection1.setBook(mockBook);
        bookUserCollection1.setUserCollection(collection1);
        bookUserCollection1.setUser(user);
        bookRepository.save(mockBook);
        bookUserCollectionRepository.save(bookUserCollection1);
        bookRepository.save(mockBook);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        userCollectionRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByEmail() {
        List<CollectionListResponseDTO> collectionList = userCollectionService.getCollectionList();

        assertThat(collectionList).size().isEqualTo(2);
        assertThat(collectionList.getFirst().getName()).isEqualTo("컬렉션");
    }

    @Transactional
    @Test
    void createCollection() {
        userCollectionService.createCollection("전쟁사");
        List<CollectionListResponseDTO> collectionList = userCollectionService.getCollectionList();
        assertThat(collectionList.getLast().getName()).isEqualTo("전쟁사");
    }

    @Transactional
    @Test
    void addBookWithSavedBook() {
        userCollectionService.createCollection("한강");
        BookSaveRequestDTO dto = BookSaveRequestDTO.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .status("READING")
            .build();
        userCollectionService.addBook(dto, "한강");

        List<CollectionListResponseDTO> collectionList = userCollectionService.getCollectionList();

        assertThat(collectionList.getLast().getName()).isEqualTo("한강");
        assertThat(collectionList.getLast().getBooksCover().getLast()).isEqualTo("image");
    }

    @Transactional
    @Test
    void addBookWithNotSavedBook() {
        userCollectionService.createCollection("한강");
        BookSaveRequestDTO dto = BookSaveRequestDTO.builder()
            .title("소년이 온다")
            .author("한강")
            .pubDate("2014-05-19")
            .description("2024 노벨문학상")
            .isbn13("9788936434120")
            .cover("image 한강")
            .categoryName("노벨문학상")
            .publisher("창비")
            .status("READING")
            .build();
        userCollectionService.addBook(dto, "한강");

        List<CollectionListResponseDTO> collectionList = userCollectionService.getCollectionList();

        assertThat(collectionList.getLast().getName()).isEqualTo("한강");
        assertThat(collectionList.getLast().getBooksCover().getLast()).isEqualTo("image 한강");
    }
}
