package com.gomdolbook.api.application.collection.web;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.bookmetacollection.command.AddBookToCollectionCommand;
import com.gomdolbook.api.application.bookmetacollection.command.AddBookToCollectionHandler;
import com.gomdolbook.api.application.bookmetacollection.command.RemoveBookFromCollectionCommand;
import com.gomdolbook.api.application.bookmetacollection.command.RemoveBookFromCollectionHandler;
import com.gomdolbook.api.application.collection.CollectionApplicationService;
import com.gomdolbook.api.application.collection.command.CollectionCreateCommand;
import com.gomdolbook.api.application.collection.command.CollectionCreateHandler;
import com.gomdolbook.api.application.collection.command.CollectionDeleteCommand;
import com.gomdolbook.api.application.collection.command.CollectionDeleteHandler;
import com.gomdolbook.api.application.collection.command.CollectionNameChangeCommand;
import com.gomdolbook.api.application.collection.command.CollectionNameChangeHandler;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.application.collection.dto.NewCollectionNameDTO;
import com.gomdolbook.api.application.shared.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequiredArgsConstructor
@RestController
public class CollectionController {

    private final BookMetaCollectionApplicationService bookMetaCollectionApplicationService;
    private final CollectionApplicationService collectionApplicationService;
    private final CollectionDeleteHandler collectionDeleteHandler;
    private final CollectionCreateHandler collectionCreateHandler;
    private final AddBookToCollectionHandler addBookToCollectionHandler;
    private final RemoveBookFromCollectionHandler removeBookFromCollectionHandler;
    private final CollectionNameChangeHandler collectionNameChangeHandler;

    @GetMapping("/v2/collections")
    public ResponseEntity<ApiResponse<List<CollectionsDTO>>> getCollectionList() {
        List<CollectionsDTO> collectionList = collectionApplicationService.getCollections();
        if (collectionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ApiResponse<List<CollectionsDTO>> response = new ApiResponse<>(
            collectionList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v2/collections/{id}")
    public ResponseEntity<ApiResponse<CollectionDetailDTO>> getCollection(
        @PathVariable Long id) {
        CollectionDetailDTO collection = collectionApplicationService.getCollection(
            id);
        ApiResponse<CollectionDetailDTO> response = new ApiResponse<>(collection);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v2/collections")
    public ResponseEntity<Object> createCollection(@RequestBody @Valid CollectionCreateCommand command) {

        collectionCreateHandler.handle(command);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/v2/collections/{name}")
            .buildAndExpand(command.name())
            .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/v2/collections/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        CollectionDeleteCommand command = new CollectionDeleteCommand(id);
        collectionDeleteHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/v2/collections/{id}")
    public ResponseEntity<Void> changeCollectionName(@RequestBody @Valid
        NewCollectionNameDTO dto, @PathVariable Long id) {
        collectionNameChangeHandler.handle(new CollectionNameChangeCommand(dto.name(),id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/collections/{id}/book")
    public ResponseEntity<Void> addBookToCollection(
        @RequestBody @Valid BookMetaSaveCommand command, @PathVariable Long id) {
        AddBookToCollectionCommand addBookToCollectionCommand = new AddBookToCollectionCommand(
            command, id);
        addBookToCollectionHandler.handle(addBookToCollectionCommand);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v2/collections/{id}/book/{isbn}")
    public ResponseEntity<Void> removeBookFromCollection(@PathVariable String isbn,
        @PathVariable Long id) {
        RemoveBookFromCollectionCommand command = new RemoveBookFromCollectionCommand(
            isbn, id);
        removeBookFromCollectionHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v2/collections/{name}/book/{isbn}/exists")
    public ResponseEntity<Object> isBookExistsInCollection(@PathVariable String name,
        @PathVariable String isbn) {
        boolean bookExistsInCollection = bookMetaCollectionApplicationService.isBookExistsInCollection(
            name, isbn);
        ApiResponse<Boolean> response = new ApiResponse<>(bookExistsInCollection);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
