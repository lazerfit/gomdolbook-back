package com.gomdolbook.api.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.gomdolbook.api.service.BookService;
import com.gomdolbook.api.service.UserCollectionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserCollectionControllerTest {

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
    private MockMvc mockMvc;

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
        userCollectionRepository.save(collection1);
        userCollectionRepository.save(collection);
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
    void getCollectionList() throws Exception{
        mockMvc.perform(get("/v1/userCollectionList"))
            .andExpect(status().isOk())
            .andDo(print());
    }
}
