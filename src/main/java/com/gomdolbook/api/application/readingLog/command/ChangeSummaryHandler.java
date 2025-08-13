package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.readingLog.ReadingLogApplicationService;
import com.gomdolbook.api.application.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChangeSummaryHandler implements CommandHandler<ChangeSummaryCommand> {

    private final ReadingLogApplicationService service;

    @Override
    public void handle(ChangeSummaryCommand command) {
        service.changeSummary(command);
    }
}
