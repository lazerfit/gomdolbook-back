package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.shared.Command;

public record ChangeSummaryCommand(String summary, Long id, String email) implements
    Command {

}
