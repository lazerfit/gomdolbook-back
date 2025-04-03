package com.gomdolbook.api.domain.models.bookCollection;

import static com.gomdolbook.api.persistence.entity.QBook.book;
import static com.gomdolbook.api.persistence.entity.QBookUserCollection.bookUserCollection;
import static com.gomdolbook.api.persistence.entity.QUser.user;
import static com.gomdolbook.api.persistence.entity.QUserCollection.userCollection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.api.dto.book.QBookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.book.QBookListResponseDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BookCollectionRepositoryImpl implements BookCollectionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookCollectionCoverData> getAllCollection(String email) {

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
    public List<BookListData> getCollection(String name, String email) {

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
    public Optional<BookCollection> find(String isbn, String name, String email) {
        BookCollection collection = queryFactory.selectFrom(bookUserCollection)
            .join(bookUserCollection.user, user)
            .join(bookUserCollection.userCollection, userCollection)
            .join(bookUserCollection.book, book)
            .where(bookUserCollection.user.email.eq(email))
            .where(bookUserCollection.userCollection.name.eq(name))
            .where(bookUserCollection.book.isbn13.eq(isbn))
            .fetchOne();

        return Optional.ofNullable(collection);
    }

}
