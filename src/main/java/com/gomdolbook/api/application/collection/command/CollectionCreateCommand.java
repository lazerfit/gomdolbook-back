package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.shared.Command;
import jakarta.validation.constraints.NotBlank;

public record CollectionCreateCommand(
    @NotBlank String name
) implements Command {

}
