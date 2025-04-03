package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record BookSaveCommand(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String pubDate,
    @NotBlank String description,
    @NotBlank String isbn13,
    @NotBlank String cover,
    @NotBlank String categoryName,
    @NotBlank String publisher,
    String status
) implements Command {

}
