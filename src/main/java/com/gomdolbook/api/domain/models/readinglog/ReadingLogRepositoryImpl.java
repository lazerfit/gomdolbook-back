package com.gomdolbook.api.domain.models.readinglog;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.readinglog.QReadingLog.readingLog;
import static com.gomdolbook.api.domain.models.user.QUser.user;

import com.gomdolbook.api.application.readingLog.dto.QReadingLogWithBookDTO;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReadingLogRepositoryImpl implements ReadingLogRepositoryCustom{

    private final JPAQueryFactory factory;

    @Override
    public Optional<ReadingLogWithBookDTO> findWithBookByIsbnAndEmail(String isbn, String email) {
        ReadingLogWithBookDTO result = factory
            .select(
                new QReadingLogWithBookDTO(
                    readingLog.id,
                    bookMeta.title,
                    bookMeta.author,
                    bookMeta.cover,
                    bookMeta.publisher,
                    book.status,
                    readingLog.summary,
                    readingLog.note,
                    readingLog.rating,
                    book.startedAt,
                    book.finishedAt
                )
            )
            .from(readingLog)
            .join(readingLog.book, book)
            .join(readingLog.book.bookMeta, bookMeta)
            .join(readingLog.book.user, user)
            .where(readingLog.book.bookMeta.isbn.eq(isbn))
            .where(readingLog.book.user.email.eq(email))
            .fetchFirst();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ReadingLogWithBookDTO> findWithBookByIdAndEmail(Long id, String email) {
        ReadingLogWithBookDTO result = factory
            .select(
                new QReadingLogWithBookDTO(
                    readingLog.id,
                    bookMeta.title,
                    bookMeta.author,
                    bookMeta.cover,
                    bookMeta.publisher,
                    book.status,
                    readingLog.summary,
                    readingLog.note,
                    readingLog.rating,
                    book.startedAt,
                    book.finishedAt
                )
            )
            .from(readingLog)
            .join(readingLog.book, book)
            .join(readingLog.book.bookMeta, bookMeta)
            .join(readingLog.book.user, user)
            .where(readingLog.id.eq(id))
            .where(readingLog.book.user.email.eq(email))
            .fetchFirst();

        return Optional.ofNullable(result);
    }
}
