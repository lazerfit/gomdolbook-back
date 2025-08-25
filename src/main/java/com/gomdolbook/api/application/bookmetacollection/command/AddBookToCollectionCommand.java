package com.gomdolbook.api.application.bookmetacollection.command;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.shared.Command;

public record AddBookToCollectionCommand(
    BookMetaSaveCommand bookMetaSaveCommand,
    Long id
) implements Command {

}
