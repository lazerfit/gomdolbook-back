package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QBook.book;
import static com.gomdolbook.api.persistence.entity.QBookUserCollection.bookUserCollection;
import static com.gomdolbook.api.persistence.entity.QUserCollection.userCollection;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.QBookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.QBookListResponseDTO;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BookUserCollectionRepositoryImpl implements BookUserCollectionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookCollectionCoverDTO> getAllCollection(String email) {

        return queryFactory.select(
            new QBookCollectionCoverDTO(userCollection.name, book.cover)
            )
            .from(userCollection)
            .leftJoin(bookUserCollection).on(userCollection.id.eq(bookUserCollection.userCollection.id))
            .leftJoin(book).on(book.id.eq(bookUserCollection.book.id))
            .where(userCollection.user.email.eq(email))
            .fetch();
    }

    @Override
    public List<BookListResponseDTO> getCollection(String name, String email) {

        return queryFactory.select(
                new QBookListResponseDTO(
                    book.cover,
                    book.title,
                    book.isbn13,
                    book.readingLog.status
                )
            ).from(userCollection)
            .leftJoin(bookUserCollection).on(userCollection.id.eq(bookUserCollection.userCollection.id))
            .leftJoin(book).on(book.id.eq(bookUserCollection.book.id))
            .where(userCollection.user.email.eq(email))
            .where(userCollection.name.eq(name))
            .fetch();
    }

    @Override
    public Optional<BookUserCollection> findByIsbnAndName(String isbn, String name, String email) {
        BookUserCollection collection = queryFactory.selectFrom(bookUserCollection)
            .where(bookUserCollection.user.email.eq(email))
            .where(bookUserCollection.userCollection.name.eq(name))
            .where(bookUserCollection.book.isbn13.eq(isbn))
            .fetchOne();

        return Optional.ofNullable(collection);
    }

}
