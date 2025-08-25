package com.gomdolbook.api.application.bookmetacollection.command;

import com.gomdolbook.api.application.shared.Command;

public record RemoveBookFromCollectionCommand(
    String isbn,
    Long id
) implements Command {

}
