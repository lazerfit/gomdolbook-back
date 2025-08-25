package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record StatusUpdateCommand(
    @NotBlank String isbn,
    @NotBlank String status
) implements Command {

}
