package com.gomdolbook.api.application.collection.command;

import com.gomdolbook.api.application.collection.CollectionApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CollectionDeleteHandler implements CommandHandler<CollectionDeleteCommand> {

    private final CollectionApplicationService service;

    @Override
    public void handle(CollectionDeleteCommand command) {
        service.deleteCollection(command.id());
    }
}
