package com.gomdolbook.api.application.collection.web;

import com.gomdolbook.api.application.shared.ApiResponse;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverListData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.application.collection.command.BookAddCommand;
import com.gomdolbook.api.application.collection.command.BookAddHandler;
import com.gomdolbook.api.application.collection.command.BookRemoveCommand;
import com.gomdolbook.api.application.collection.command.BookRemoveHandler;
import com.gomdolbook.api.application.collection.command.CollectionCreateCommand;
import com.gomdolbook.api.application.collection.command.CollectionCreateHandler;
import com.gomdolbook.api.application.collection.command.CollectionDeleteCommand;
import com.gomdolbook.api.application.collection.command.CollectionDeleteHandler;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequiredArgsConstructor
@Controller
public class CollectionController {

    private final BookCollectionApplicationService bookCollectionApplicationService;
    private final BookAddHandler bookAddHandler;
    private final CollectionDeleteHandler collectionDeleteHandler;
    private final BookRemoveHandler bookRemoveHandler;
    private final CollectionCreateHandler collectionCreateHandler;

    @GetMapping("/v1/collection/list")
    public ResponseEntity<ApiResponse<List<BookCollectionCoverListData>>> getCollectionList() {
        List<BookCollectionCoverListData> collectionList = bookCollectionApplicationService.getCollectionList();
        if (collectionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ApiResponse<List<BookCollectionCoverListData>> response = new ApiResponse<>(
            collectionList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v1/collection/{name}")
    public ResponseEntity<ApiResponse<List<BookListData>>> getCollection(
        @PathVariable String name) {
        var collection = bookCollectionApplicationService.getCollection(name);
        ApiResponse<List<BookListData>> response = new ApiResponse<>(collection);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v1/collection/create")
    public ResponseEntity<Object> createCollection(@RequestParam("name") String name) {
        CollectionCreateCommand command = new CollectionCreateCommand(name);
        collectionCreateHandler.handle(command);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/v1/collection/{name}")
            .buildAndExpand(name)
            .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/v1/collection/delete")
    public ResponseEntity<Void> deleteCollection(@RequestParam("name") String name) {
        CollectionDeleteCommand command = new CollectionDeleteCommand(name);
        collectionDeleteHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/collection/{name}/book/add")
    public ResponseEntity<Void> addBook(@RequestBody @Validated BookSaveCommand command, @PathVariable String name) {
        BookAddCommand bookAddCommand = new BookAddCommand(command, name);
        bookAddHandler.handle(bookAddCommand);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v1/collection/{name}/book/remove")
    public ResponseEntity<Void> removeBook(@RequestParam("isbn") String isbn,
        @PathVariable String name) {
        BookRemoveCommand command = new BookRemoveCommand(isbn, name);
        bookRemoveHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}
