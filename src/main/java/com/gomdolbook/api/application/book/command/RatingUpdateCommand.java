package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record RatingUpdateCommand(
    @NotBlank String isbn,
    @NotBlank int star
) implements Command {

}
