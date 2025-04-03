package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.bookCollection.BookCollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookAddHandler implements CommandHandler<BookAddCommand> {

    private final BookCollectionApplicationService bookCollectionService;

    @Override
    public void handle(BookAddCommand command) {
        bookCollectionService.addBook(command.saveCommand(), command.collectionName());
    }
}
