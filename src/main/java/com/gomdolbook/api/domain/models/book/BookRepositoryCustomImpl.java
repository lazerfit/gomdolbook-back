package com.gomdolbook.api.domain.models.book;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.readinglog.QReadingLog.readingLog;
import static com.gomdolbook.api.domain.models.user.QUser.user;

import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.FinishedBookCalendarData;
import com.gomdolbook.api.application.book.dto.QBookListData;
import com.gomdolbook.api.application.book.dto.QFinishedBookCalendarData;
import com.gomdolbook.api.domain.models.book.Book.Status;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.QBookMeta;
import com.gomdolbook.api.domain.models.user.QUser;
import com.gomdolbook.api.domain.models.user.User;
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
    public List<BookListData> findLibraryByStatus(Status status, String email) {
        return queryFactory.select(
                new QBookListData(book.bookMeta.cover, book.bookMeta.title, book.bookMeta.isbn,
                    book.status, book.readingLog.id)
            ).from(book)
            .join(book.bookMeta, bookMeta)
            .join(book.user, user)
            .join(book.readingLog, readingLog)
            .where(book.user.email.eq(email), book.status.eq(status))
            .fetch();
    }

    @Override
    public Optional<Status> findStatus(String isbn, String email) {
        return Optional.ofNullable(queryFactory.select(book.status)
            .from(book)
            .join(book.bookMeta, bookMeta)
            .where(book.user.email.eq(email).and(book.bookMeta.isbn.eq(isbn)))
            .fetchFirst());
    }

    @Override
    public List<FinishedBookCalendarData> findFinishedBookCalendarData(String email) {
        return queryFactory.select(
                new QFinishedBookCalendarData(book.bookMeta.title,book.bookMeta.isbn, book.bookMeta.cover, book.readingLog.rating,book.finishedAt)
            ).from(book)
            .join(book.bookMeta, bookMeta)
            .join(book.readingLog, readingLog)
            .join(book.user, user)
            .where(book.user.email.eq(email).and(book.status.eq(Status.FINISHED)))
            .fetch();
    }

    @Override
    public Optional<Book> findByUserAndBookMeta(User user, BookMeta bookMeta) {
        Book result = queryFactory.select(book)
            .from(book)
            .join(book.user, QUser.user)
            .join(book.bookMeta, QBookMeta.bookMeta)
            .where(book.user.email.eq(user.getEmail()), book.bookMeta.id.eq(bookMeta.getId()))
            .fetchOne();

        return Optional.ofNullable(result);
    }


}
