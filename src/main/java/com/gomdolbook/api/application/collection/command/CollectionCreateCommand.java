package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.shared.Command;

public record CollectionCreateCommand(String name) implements Command {

}
