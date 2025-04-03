package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingUpdateHandler implements CommandHandler<RatingUpdateCommand> {

    private final BookApplicationService bookService;

    @Override
    public void handle(RatingUpdateCommand command) {
        bookService.changeRating(command.star(), command.isbn());
    }
}
