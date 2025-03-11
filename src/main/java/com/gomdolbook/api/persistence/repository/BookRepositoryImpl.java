package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QBook.book;
import static com.gomdolbook.api.persistence.entity.QReadingLog.readingLog;

import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.QBookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.QBookListResponseDTO;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookAndReadingLogDTO> findByUserEmailAndIsbn(String email, String isbn) {

        BookAndReadingLogDTO dto = queryFactory.select(
                new QBookAndReadingLogDTO(book)).from(book)
            .join(book.readingLog, readingLog).fetchJoin()
            .where(book.isbn13.eq(isbn))
            .where(book.readingLog.user.email.eq(email))
            .fetchFirst();

        return Optional.ofNullable(dto);
    }

    @Override
    public List<BookListResponseDTO> findByReadingStatus(Status status, String email) {
        return queryFactory.select(
                new QBookListResponseDTO(book.cover, book.title, book.isbn13, book.readingLog.status)
            ).from(book)
            .join(book.readingLog,readingLog)
            .where(book.readingLog.user.email.eq(email))
            .where(book.readingLog.status.eq(status))
            .fetch();
    }

    @Override
    public Optional<Status> getStatus(String isbn, String email) {
        return Optional.ofNullable(queryFactory.select(book.readingLog.status)
            .from(book)
            .join(book.readingLog, readingLog)
            .where(book.readingLog.user.email.eq(email))
            .where(book.isbn13.eq(isbn))
            .fetchOne());
    }
}
