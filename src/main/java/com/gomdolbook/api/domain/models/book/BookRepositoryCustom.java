package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.user.User;
import java.util.List;
import java.util.Optional;

public interface BookRepositoryCustom {

    List<BookListData> findLibraryByStatus(Status status, String email);
    Optional<Status> findStatus(String isbn, String email);
    List<FinishedBookCalendarData> findFinishedBookCalendarData(String email);
    Optional<Book> findByUserAndBookMeta(User user, BookMeta bookMeta);
}
