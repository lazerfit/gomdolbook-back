package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import java.util.List;
import java.util.Optional;

public interface BookRepositoryCustom {

    Optional<BookAndReadingLogData> findByEmail(String email, String isbn);
    List<BookListData> findByStatus(Status status, String email);
    Optional<Status> getStatus(String isbn, String email);
    List<FinishedBookCalendarData> getFinishedBookCalendarData(String email);

}
