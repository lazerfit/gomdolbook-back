package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CollectionDeleteHandler implements CommandHandler<CollectionDeleteCommand> {

    private final BookCollectionApplicationService bookCollectionService;

    @Override
    public void handle(CollectionDeleteCommand command) {
        bookCollectionService.deleteCollection(command.name());
    }
}
