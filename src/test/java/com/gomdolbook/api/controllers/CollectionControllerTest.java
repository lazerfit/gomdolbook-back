package com.gomdolbook.api.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.dto.BookSaveRequestDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookCollection.BookCollectionRepository;
import com.gomdolbook.api.domain.models.readingLog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CollectionControllerTest {

    static Collection collection;
    static Book mockBook;
    static User user;

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
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookCollectionRepository bookCollectionRepository;

    @Autowired
    TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("redkafe@daum.net", "image");
        ReadingLog savedReadingLog = testDataFactory.createReadingLog(user);
        mockBook = testDataFactory.createBook(savedReadingLog);
        collection = testDataFactory.createUserCollection("컬렉션", user);
        testDataFactory.createBookUserCollection(mockBook, collection, user);
    }

    @AfterEach
    void tearDown() {
        bookCollectionRepository.deleteAll();
        bookRepository.deleteAll();
        collectionRepository.deleteAll();
        readingLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getCollectionList() throws Exception{
        mockMvc.perform(get("/v1/collection/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("컬렉션"))
            .andDo(print());
    }

    @Test
    void createCollection() throws Exception{
        mockMvc.perform(post("/v1/collection/create")
                .param("name", "한강"))
            .andExpect(status().isCreated())
            .andDo(print());
    }

    @Test
    void getCollection() throws Exception {
        mockMvc.perform(get("/v1/collection/{name}", "컬렉션"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].cover").value("image"))
            .andExpect(jsonPath("$.data[0].title").value("펠로폰네소스 전쟁사"))
            .andDo(print());
    }

    @Test
    void addBook() throws Exception{
        BookSaveRequestDTO requestDTO = BookSaveRequestDTO.builder()
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

        String data = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/v1/collection/{name}/book/add", "컬렉션")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void removeBook() throws Exception{
        mockMvc.perform(delete("/v1/collection/{name}/book/remove", "컬렉션")
                .param("isbn", "9788991290402"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void deleteCollection() throws Exception {
        mockMvc.perform(delete("/v1/collection/delete")
                .param("name", "컬렉션"))
            .andExpect(status().isOk())
            .andDo(print());
    }
}
