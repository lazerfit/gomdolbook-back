package com.gomdolbook.api.application.collection.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.bookmetacollection.command.AddBookToCollectionHandler;
import com.gomdolbook.api.application.bookmetacollection.command.RemoveBookFromCollectionHandler;
import com.gomdolbook.api.application.collection.CollectionApplicationService;
import com.gomdolbook.api.application.collection.command.CollectionCreateCommand;
import com.gomdolbook.api.application.collection.command.CollectionCreateHandler;
import com.gomdolbook.api.application.collection.command.CollectionDeleteHandler;
import com.gomdolbook.api.application.collection.command.CollectionNameChangeHandler;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.application.collection.dto.NewCollectionNameDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockCustomUser
@WebMvcTest(CollectionController.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CollectionApplicationService collectionApplicationService;
    @MockitoBean
    private BookMetaCollectionApplicationService bookMetaCollectionApplicationService;
    @MockitoBean
    private CollectionDeleteHandler collectionDeleteHandler;
    @MockitoBean
    private CollectionCreateHandler collectionCreateHandler;
    @MockitoBean
    private AddBookToCollectionHandler addBookToCollectionHandler;
    @MockitoBean
    private RemoveBookFromCollectionHandler removeBookFromCollectionHandler;
    @MockitoBean
    private CollectionNameChangeHandler collectionNameChangeHandler;

    @Test
    void getCollectionList() throws Exception {
        CollectionsDTO collectionsDTO = new CollectionsDTO(1L, "name", List.of("cover"));
        given(collectionApplicationService.getCollections()).willReturn(List.of(collectionsDTO));

        mockMvc.perform(get("/v2/collections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("name"));
    }

    @Test
    void getCollectionList_Empty() throws Exception {
        given(collectionApplicationService.getCollections()).willReturn(List.of());

        mockMvc.perform(get("/v2/collections"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getCollection() throws Exception {
        BookInfoInCollectionDTO dto = new BookInfoInCollectionDTO("t", "c",
            "i");
        CollectionDetailDTO detailDTO = new CollectionDetailDTO(1L, "name", List.of(dto));
        given(collectionApplicationService.getCollection(1L)).willReturn(detailDTO);

        mockMvc.perform(get("/v2/collections/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.collectionName").value("name"))
            .andExpect(jsonPath("$.data.books[0].title").value("t"));
    }

    @Test
    void createCollection() throws Exception {
        CollectionCreateCommand command = new CollectionCreateCommand("name");

        mockMvc.perform(post("/v2/collections")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
                .with(csrf()))
            .andExpect(status().isCreated());
    }

    @Test
    void deleteCollection() throws Exception {
        mockMvc.perform(delete("/v2/collections/{id}", 1)
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    void changeCollectionName() throws Exception {
        NewCollectionNameDTO dto = new NewCollectionNameDTO("name");

        mockMvc.perform(patch("/v2/collections/{id}", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void changeCollectionName_Valid_Fail() throws Exception {
        NewCollectionNameDTO dto = new NewCollectionNameDTO("  ");

        mockMvc.perform(patch("/v2/collections/{id}", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addBookToCollection() throws Exception {
        BookSaveCommand command = new BookSaveCommand("t", "a", "p",
            "d", "i", "c", "ca", "p", "READING");

        mockMvc.perform(post("/v2/collections/{id}/book", 1)
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isOk());
    }

    @Test
    void addBookToCollection_Valid_Fail() throws Exception {
        BookSaveCommand command = new BookSaveCommand("t", "a", "p",
            "d", "i", "c", "  ", "p", "READING");

        mockMvc.perform(post("/v2/collections/{id}/book", 1)
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void removeBookFromCollection() throws Exception {
        mockMvc.perform(delete("/v2/collections/{id}/book/{isbn}", 1, "isbn")
                .with(csrf()))
            .andExpect(status().isOk());
    }
}
