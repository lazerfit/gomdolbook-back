package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSaveHandler implements CommandHandler<BookSaveCommand> {

    private final BookApplicationService bookService;

    public void handle(BookSaveCommand command) {
        bookService.addBookToLibrary(command);
    }
}
