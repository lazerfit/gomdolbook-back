package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record BookMetaSaveCommand(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String pubDate,
    @NotBlank String description,
    @NotBlank String isbn,
    @NotBlank String cover,
    @NotBlank String categoryName,
    @NotBlank String publisher
)  implements Command {

}
