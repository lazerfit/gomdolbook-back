package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog.Status;
import java.util.List;
import java.util.Optional;


public interface BookRepositoryCustom {

    Optional<BookAndReadingLogData> find(String email, String isbn);
    List<BookListData> find(Status status, String email);
    Optional<Status> getStatus(String isbn, String email);
}
