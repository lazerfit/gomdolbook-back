package com.gomdolbook.api.application.book.command;

import com.gomdolbook.api.application.shared.CommandHandler;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookMetaSaveHandler implements CommandHandler<BookMetaSaveCommand> {

    private final BookMetaRepository bookMetaRepository;

    @Override
    public void handle(BookMetaSaveCommand command) {
        bookMetaRepository.save(BookMeta.of(command));
    }
}
