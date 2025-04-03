package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.shared.Command;

public record CollectionDeleteCommand(String name) implements Command {

}
