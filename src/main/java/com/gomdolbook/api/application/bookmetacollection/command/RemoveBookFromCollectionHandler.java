package com.gomdolbook.api.application.bookmetacollection.command;

import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RemoveBookFromCollectionHandler implements
    CommandHandler<RemoveBookFromCollectionCommand> {

    private final BookMetaCollectionApplicationService service;

    @Override
    public void handle(RemoveBookFromCollectionCommand command) {
        service.removeBookFromCollection(command.isbn(), command.collectionName());
    }
}
