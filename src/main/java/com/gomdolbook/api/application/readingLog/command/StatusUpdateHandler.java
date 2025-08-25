package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StatusUpdateHandler implements CommandHandler<StatusUpdateCommand> {

    private final BookApplicationService bookService;

    @Override
    public void handle(StatusUpdateCommand command) {
        bookService.changeStatus(command.isbn(), command.status());
    }
}
