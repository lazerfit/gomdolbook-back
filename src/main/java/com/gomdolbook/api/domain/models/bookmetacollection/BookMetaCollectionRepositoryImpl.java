package com.gomdolbook.api.domain.models.bookmetacollection;

import static com.gomdolbook.api.domain.models.book.QBook.book;
import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.bookmetacollection.QBookMetaCollection.bookMetaCollection;
import static com.gomdolbook.api.domain.models.collection.QCollection.collection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.QBookCollectionCoverData;
import com.gomdolbook.api.application.bookmetacollection.dto.CollectionBookMetaData;
import com.gomdolbook.api.application.bookmetacollection.dto.QCollectionBookMetaData;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BookMetaCollectionRepositoryImpl implements BookMetaCollectionRepositoryCustom{

    private final JPAQueryFactory factory;

    @Override
    public List<BookCollectionCoverData> getAllCollection(String email) {
        return factory.select(
                new QBookCollectionCoverData(collection.name, book.cover)
            )
            .from(collection)
            .leftJoin(bookMetaCollection).on(collection.id.eq(bookMetaCollection.collection.id))
            .leftJoin(bookMeta).on(bookMeta.id.eq(bookMetaCollection.bookMeta.id))
            .where(collection.user.email.eq(email))
            .fetch();
    }

    @Override
    public List<CollectionBookMetaData> getCollectionData(String email, String collectionName) {
        return factory.select(
            new QCollectionBookMetaData(
                bookMeta.cover,
                bookMeta.title,
                bookMeta.isbn
            ))
            .from(collection)
            .leftJoin(bookMetaCollection).on(collection.id.eq(bookMetaCollection.collection.id))
            .leftJoin(bookMeta).on(bookMeta.id.eq(bookMetaCollection.bookMeta.id))
            .where(collection.user.email.eq(email))
            .where(collection.name.eq(collectionName))
            .where(bookMetaCollection.isNotNull())
            .fetch();
    }
}
