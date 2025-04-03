package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.shared.Command;

public record BookAddCommand(BookSaveCommand saveCommand, String collectionName) implements
    Command {

}
