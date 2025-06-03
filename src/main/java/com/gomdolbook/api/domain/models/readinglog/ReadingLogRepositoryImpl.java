package com.gomdolbook.api.domain.models.readinglog;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.readinglog.QReadingLog.readingLog;
import static com.gomdolbook.api.domain.models.user.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReadingLogRepositoryImpl implements ReadingLogRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Integer getRating(String isbn, String email) {
        Integer result = queryFactory.select(readingLog.rating).from(readingLog)
            .join(readingLog.user, user)
            .join(readingLog.book, book)
            .where(readingLog.user.email.eq(email))
            .where(readingLog.book.bookMeta.isbn.eq(isbn))
            .fetchOne();

        return Optional.ofNullable(result).orElse(0);
    }
}
