package com.gomdolbook.api.application.shared;

public interface CommandHandler<C extends Command> {

    void handle(C command);
}
