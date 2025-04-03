package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CollectionCreateHandler implements CommandHandler<CollectionCreateCommand> {

    private final BookCollectionApplicationService bookCollectionService;

    @Override
    public void handle(CollectionCreateCommand command) {
        bookCollectionService.createCollection(command.name());
    }
}
