package com.gomdolbook.api.application.collection.web;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverListData;
import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.bookmetacollection.command.AddBookToCollectionCommand;
import com.gomdolbook.api.application.bookmetacollection.command.AddBookToCollectionHandler;
import com.gomdolbook.api.application.bookmetacollection.command.RemoveBookFromCollectionCommand;
import com.gomdolbook.api.application.bookmetacollection.command.RemoveBookFromCollectionHandler;
import com.gomdolbook.api.application.bookmetacollection.dto.CollectionBookMetaData;
import com.gomdolbook.api.application.collection.command.CollectionCreateCommand;
import com.gomdolbook.api.application.collection.command.CollectionCreateHandler;
import com.gomdolbook.api.application.collection.command.CollectionDeleteCommand;
import com.gomdolbook.api.application.collection.command.CollectionDeleteHandler;
import com.gomdolbook.api.application.shared.ApiResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequiredArgsConstructor
@RestController
public class CollectionController {

    private final BookMetaCollectionApplicationService bookMetaCollectionApplicationService;
    private final CollectionDeleteHandler collectionDeleteHandler;
    private final CollectionCreateHandler collectionCreateHandler;
    private final AddBookToCollectionHandler addBookToCollectionHandler;
    private final RemoveBookFromCollectionHandler removeBookFromCollectionHandler;

    @GetMapping("/v2/collections")
    public ResponseEntity<ApiResponse<List<BookCollectionCoverListData>>> getCollectionList() {
        List<BookCollectionCoverListData> collectionList = bookMetaCollectionApplicationService.getCollectionList();
        if (collectionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ApiResponse<List<BookCollectionCoverListData>> response = new ApiResponse<>(
            collectionList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v2/collections/{name}")
    public ResponseEntity<ApiResponse<List<CollectionBookMetaData>>> getCollection(
        @PathVariable String name) {
        List<CollectionBookMetaData> collection = bookMetaCollectionApplicationService.getCollection(
            name);
        ApiResponse<List<CollectionBookMetaData>> response = new ApiResponse<>(collection);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v2/collections")
    public ResponseEntity<Object> createCollection(@RequestBody @Validated CollectionCreateCommand command) {

        collectionCreateHandler.handle(command);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/v2/collections/{name}")
            .buildAndExpand(command.name())
            .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/v2/collections/{name}")
    public ResponseEntity<Void> deleteCollection(@PathVariable String name) {
        CollectionDeleteCommand command = new CollectionDeleteCommand(name);
        collectionDeleteHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/collections/{name}/book")
    public ResponseEntity<Void> addBookToCollection(
        @RequestBody @Validated BookMetaSaveCommand command, @PathVariable String name) {
        AddBookToCollectionCommand addBookToCollectionCommand = new AddBookToCollectionCommand(
            command, name);
        addBookToCollectionHandler.handle(addBookToCollectionCommand);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v2/collections/{name}/book/{isbn}")
    public ResponseEntity<Void> removeBookFromCollection(@PathVariable String isbn,
        @PathVariable String name) {
        RemoveBookFromCollectionCommand command = new RemoveBookFromCollectionCommand(
            isbn, name);
        removeBookFromCollectionHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}
