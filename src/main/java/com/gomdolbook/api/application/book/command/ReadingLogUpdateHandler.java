package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReadingLogUpdateHandler implements CommandHandler<ReadingLogUpdateCommand> {

    private final BookApplicationService bookService;

    @Override
    public void handle(ReadingLogUpdateCommand command) {
        bookService.changeReadingLog(command);
    }

}
