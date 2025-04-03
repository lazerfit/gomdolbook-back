package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record StatusUpdateCommand(
    @NotBlank String isbn,
    @NotBlank String status
) implements Command {

}
