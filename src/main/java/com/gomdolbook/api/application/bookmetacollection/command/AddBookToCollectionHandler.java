package com.gomdolbook.api.application.bookmetacollection.command;

import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AddBookToCollectionHandler implements CommandHandler<AddBookToCollectionCommand> {

    private final BookMetaCollectionApplicationService service;

    @Override
    public void handle(AddBookToCollectionCommand command) {
        service.addBookToCollection(command.bookMetaSaveCommand(), command.id() );
    }
}
