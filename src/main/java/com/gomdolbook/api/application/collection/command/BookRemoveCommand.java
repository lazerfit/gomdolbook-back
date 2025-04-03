package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.shared.Command;

public record BookRemoveCommand(
    String isbn,
    String collectionName) implements Command {

}
