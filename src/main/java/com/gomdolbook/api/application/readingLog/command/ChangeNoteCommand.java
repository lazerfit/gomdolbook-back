package com.gomdolbook.api.application.readingLog.command;

import com.gomdolbook.api.application.shared.Command;

public record ChangeNoteCommand(String note, Long id, String email) implements
    Command {

}
