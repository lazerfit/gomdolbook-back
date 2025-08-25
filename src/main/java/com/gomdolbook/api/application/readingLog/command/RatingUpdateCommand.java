package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.shared.Command;

public record RatingUpdateCommand(
    Long id,
    int star
) implements Command {

}
