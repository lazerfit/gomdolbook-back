package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record ReadingLogUpdateCommand(
    @NotBlank String isbn,
    @NotBlank String note,
    @NotBlank String text
) implements Command {

}
