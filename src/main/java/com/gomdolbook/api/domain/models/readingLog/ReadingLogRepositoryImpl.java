package com.gomdolbook.api.domain.models.readingLog;

import static com.gomdolbook.api.persistence.entity.QBook.book;
import static com.gomdolbook.api.persistence.entity.QReadingLog.readingLog;
import static com.gomdolbook.api.persistence.entity.QUser.user;

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
            .where(readingLog.book.isbn13.eq(isbn))
            .fetchOne();

        return Optional.ofNullable(result).orElse(0);
    }
}
