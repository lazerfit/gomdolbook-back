package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QBook.book;

import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.QBookAndReadingLogDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookAndReadingLogDTO> findByUserEmailAndIsbn(String email, String isbn) {

        BookAndReadingLogDTO dto = queryFactory.select(
                new QBookAndReadingLogDTO(book)).from(book)
            .where(book.isbn13.eq(isbn))
            .where(book.readingLog.user.email.eq(email))
            .fetchFirst();

        return Optional.ofNullable(dto);
    }
}
