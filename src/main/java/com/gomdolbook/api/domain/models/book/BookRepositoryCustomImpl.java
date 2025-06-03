package com.gomdolbook.api.domain.models.book;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.readinglog.QReadingLog.readingLog;

import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.QBookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.QBookListData;
import com.gomdolbook.api.application.book.dto.QFinishedBookCalendarData;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookAndReadingLogData> findByEmail(String email, String isbn) {

        BookAndReadingLogData dto = queryFactory.select(
                new QBookAndReadingLogData(book)).from(book)
            .join(book.readingLog, readingLog)
            .join(book.bookMeta, bookMeta)
            .where(book.bookMeta.isbn.eq(isbn).and(book.readingLog.user.email.eq(email)))
            .fetchFirst();

        return Optional.ofNullable(dto);
    }

    @Override
    public List<BookListData> findByStatus(Status status, String email) {
        return queryFactory.select(
                new QBookListData(book.bookMeta.cover, book.bookMeta.title, book.bookMeta.isbn, book.readingLog.status)
            ).from(book)
            .join(book.readingLog,readingLog)
            .join(book.bookMeta, bookMeta)
            .where(book.readingLog.user.email.eq(email).and(book.readingLog.status.eq(status)))
            .fetch();
    }

    @Override
    public Optional<Status> getStatus(String isbn, String email) {
        return Optional.ofNullable(queryFactory.select(book.readingLog.status)
            .from(book)
            .join(book.readingLog, readingLog)
            .where(book.readingLog.user.email.eq(email).and(book.bookMeta.isbn.eq(isbn)))
            .fetchOne());
    }

    @Override
    public List<FinishedBookCalendarData> getFinishedBookCalendarData(String email) {
        return queryFactory.select(
                new QFinishedBookCalendarData(book.bookMeta.title,book.bookMeta.isbn, book.bookMeta.cover, book.readingLog.rating,book.finishedAt)
            ).from(book)
            .join(book.bookMeta, bookMeta)
            .where(book.readingLog.user.email.eq(email).and(book.readingLog.status.eq(Status.FINISHED)))
            .fetch();
    }
}
