package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.collection.CollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CollectionCreateHandler implements CommandHandler<CollectionCreateCommand> {

    private final CollectionApplicationService service;

    @Override
    public void handle(CollectionCreateCommand command) {
        service.createCollection(command.name());
    }
}
