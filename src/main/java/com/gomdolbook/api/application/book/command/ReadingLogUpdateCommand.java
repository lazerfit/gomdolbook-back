package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReadingLogUpdateCommand(
    @NotNull String isbn,
    @NotNull String note,
    @NotBlank String text
) implements Command {

}
