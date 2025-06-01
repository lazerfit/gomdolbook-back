package com.gomdolbook.api.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.collection.command.CollectionCreateCommand;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollection;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@WithMockCustomUser
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CollectionControllerTest {

    static User user;

    @Autowired
    UserRepository userRepository;
    @Autowired
    CollectionRepository collectionRepository;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BookMetaRepository bookMetaRepository;
    @Autowired
    BookMetaCollectionRepository bookMetaCollectionRepository;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("redkafe@daum.net", "pic1", Role.USER));
        collectionRepository.save(Collection.of(user, "컬렉션"));
    }

    @Test
    void getCollectionList_정상동작() throws Exception {
        mockMvc.perform(get("/v2/collections"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void getCollection_정상동작() throws Exception {
        mockMvc.perform(get("/v2/collections/{name}", "컬렉션"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andDo(print());
    }

    @Test
    void getCollectionV2_잘못된컬렉션이름_error() throws Exception {
        mockMvc.perform(get("/v2/collections/{name}", "오류"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andDo(print());
    }

    @Test
    void createCollection_정상동작() throws Exception {
        CollectionCreateCommand command = new CollectionCreateCommand("test1");
        mockMvc.perform(post("/v2/collections")
                .content(objectMapper.writeValueAsString(command))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(print());
    }

    @Test
    void createCollection_body_name_누락_error() throws Exception {
        CollectionCreateCommand command = new CollectionCreateCommand(null);
        mockMvc.perform(post("/v2/collections")
                .content(objectMapper.writeValueAsString(command))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("name: must not be blank"))
            .andDo(print());
    }

    @Test
    void addBookToCollection_정상동작() throws Exception {
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목", "저자",
            "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");
        mockMvc.perform(post("/v2/collections/{name}/book", "컬렉션")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void addBookToCollection_필수항목누락_error() throws Exception {
        BookMetaSaveCommand command = new BookMetaSaveCommand(null, "저자",
            "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");
        mockMvc.perform(post("/v2/collections/{name}/book", "컬렉션")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("title: must not be blank"))
            .andDo(print());
    }

    @Test
    void deleteCollection_정상동작() throws Exception {
        mockMvc.perform(delete("/v2/collections/{name}", "컬렉션"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void deleteCollection_존재하지않는_컬렉션_error() throws Exception {
        mockMvc.perform(delete("/v2/collections/{name}", "test11"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("Can't find Collection: 해당 컬렉션을 찾을 수 없습니다."))
            .andDo(print());
    }

    @Test
    void removeBookFromCollection_정상동작() throws Exception {
        Collection collection1 = collectionRepository.save(Collection.of(user, "삭제"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목1", "저자1",
            "2025-01-01", "설명1", "2345678901234", "cover1", "카테고리", "출판사");
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta,collection1,user));

        mockMvc.perform(delete("/v2/collections/{name}/book/{isbn}", "삭제", "2345678901234"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void removeBookFromCollection_없는_ISBN_error() throws Exception {
        Collection collection1 = collectionRepository.save(Collection.of(user, "삭제"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목1", "저자1",
            "2025-01-01", "설명1", "2345678901234", "cover1", "카테고리", "출판사");
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta,collection1,user));

        mockMvc.perform(delete("/v2/collections/{name}/book/{isbn}", "삭제", "99999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("Can't find Book: 책을 찾을 수 없습니다: 99999"))
            .andDo(print());
    }

    @Test
    void removeBookFromCollection_들어있지않은책_error() throws Exception {
        Collection collection1 = collectionRepository.save(Collection.of(user, "삭제"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목1", "저자1",
            "2025-01-01", "설명1", "2345678901234", "cover1", "카테고리", "출판사");
        BookMetaSaveCommand command2 = new BookMetaSaveCommand("제목1", "저자1",
            "2025-01-01", "설명1", "99999", "cover1", "카테고리", "출판사");
        bookMetaRepository.save(BookMeta.of(command2));
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta,collection1,user));

        mockMvc.perform(delete("/v2/collections/{name}/book/{isbn}", "삭제", "99999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors[0]").value("해당 컬렉션에 등록되어있지 않은 책입니다."))
            .andDo(print());
    }

    @Test
    void isBookExistsInCollection() throws Exception {
        Collection collection = collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목1", "저자1",
            "2025-01-01", "설명1", "2345678901234", "cover1", "카테고리", "출판사");
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta,collection,user));

        mockMvc.perform(get("/v2/collections/{name}/book/{isbn}/exists", "내컬렉션", "2345678901234"))
            .andExpect(status().isOk())
            .andDo(print());
    }

}
