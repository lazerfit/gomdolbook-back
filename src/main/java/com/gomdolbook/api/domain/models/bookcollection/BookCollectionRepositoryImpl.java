package com.gomdolbook.api.domain.models.bookcollection;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.bookcollection.QBookCollection.bookCollection;
import static com.gomdolbook.api.domain.models.collection.QCollection.collection;
import static com.gomdolbook.api.domain.models.user.QUser.user;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.QBookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.QBookListData;
import com.gomdolbook.api.domain.models.collection.QCollection;
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
            new QBookCollectionCoverData(collection.name, book.cover)
            )
            .from(collection)
            .leftJoin(bookCollection).on(collection.id.eq(bookCollection.collection.id))
            .leftJoin(book).on(book.id.eq(bookCollection.book.id))
            .where(collection.user.email.eq(email))
            .fetch();
    }

    @Override
    public List<BookListData> getCollection(String name, String email) {

        return queryFactory.select(
                new QBookListData(
                    book.bookMeta.cover,
                    book.bookMeta.title,
                    book.bookMeta.isbn,
                    book.readingLog.status
                )
            ).from(collection)
            .leftJoin(bookCollection).on(collection.id.eq(bookCollection.collection.id))
            .leftJoin(book).on(book.id.eq(bookCollection.book.id))
            .where(collection.user.email.eq(email))
            .where(collection.name.eq(name))
            .fetch();
    }

    @Override
    public Optional<BookCollection> find(String isbn, String name, String email) {
        BookCollection collection = queryFactory.selectFrom(bookCollection)
            .join(bookCollection.user, user)
            .join(bookCollection.collection, QCollection.collection)
            .join(bookCollection.book, book)
            .where(bookCollection.user.email.eq(email))
            .where(bookCollection.collection.name.eq(name))
            .where(bookCollection.book.isbn.eq(isbn))
            .fetchOne();

        return Optional.ofNullable(collection);
    }

}
