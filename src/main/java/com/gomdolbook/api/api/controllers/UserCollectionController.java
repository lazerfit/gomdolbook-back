package com.gomdolbook.api.api.controllers;

import com.gomdolbook.api.api.dto.APIResponseDTO;
import com.gomdolbook.api.api.dto.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.service.BookUserCollectionService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequiredArgsConstructor
@Controller
public class UserCollectionController {

    private final BookUserCollectionService bookUserCollectionService;

    @GetMapping("/v1/collection/list")
    public ResponseEntity<APIResponseDTO<List<BookCollectionCoverListResponseDTO>>> getCollectionList() {
        List<BookCollectionCoverListResponseDTO> collectionList = bookUserCollectionService.getCollectionList();
        if (collectionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        APIResponseDTO<List<BookCollectionCoverListResponseDTO>> response = new APIResponseDTO<>(
            collectionList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v1/collection/{name}")
    public ResponseEntity<APIResponseDTO<List<BookListResponseDTO>>> getCollection(
        @PathVariable String name) {
        var collection = bookUserCollectionService.getCollection(name);

        APIResponseDTO<List<BookListResponseDTO>> response = new APIResponseDTO<>(collection);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v1/collection/create")
    public ResponseEntity<Object> createCollection(@RequestParam("name") String name) {
        bookUserCollectionService.createCollection(name);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/v1/collection/{name}")
            .buildAndExpand(name)
            .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/v1/collection/{name}/book/add")
    public ResponseEntity<Void> addBook(@RequestBody BookSaveRequestDTO dto, @PathVariable String name) {
        bookUserCollectionService.addBook(dto, name);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v1/collection/{name}/book/remove")
    public ResponseEntity<Void> deleteBook(@RequestParam("isbn") String isbn,
        @PathVariable String name) {
            bookUserCollectionService.deleteBook(isbn, name);
        return ResponseEntity.ok().build();
    }
}
