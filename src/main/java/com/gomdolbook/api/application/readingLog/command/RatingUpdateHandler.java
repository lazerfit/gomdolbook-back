package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.readingLog.ReadingLogApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import com.gomdolbook.api.domain.shared.InvalidStarValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingUpdateHandler implements CommandHandler<RatingUpdateCommand> {

    private final ReadingLogApplicationService bookService;

    @Override
    public void handle(RatingUpdateCommand command) {
        if (command.star() < 1 || command.star() > 5) {
            throw new InvalidStarValueException("star 값이 비정상적입니다.");
        }
        bookService.changeRating(command.star(), command.id());
    }
}
