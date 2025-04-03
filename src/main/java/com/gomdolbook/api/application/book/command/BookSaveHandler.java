package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.book.BookApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSaveHandler {

    private final BookApplicationService bookService;

    public void handle(BookSaveCommand command) {
        bookService.saveBook(command);
    }
}
