package com.gomdolbook.api.domain.models.bookmetacollection;

import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.bookmetacollection.QBookMetaCollection.bookMetaCollection;
import static com.gomdolbook.api.domain.models.collection.QCollection.collection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.QBookCollectionCoverData;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.QBookInfoInCollectionDTO;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.querydsl.core.Tuple;
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
                new QBookCollectionCoverData(collection.id,collection.name, bookMeta.cover)
            )
            .from(collection)
            .leftJoin(bookMetaCollection).on(collection.id.eq(bookMetaCollection.collection.id))
            .leftJoin(bookMeta).on(bookMeta.id.eq(bookMetaCollection.bookMeta.id))
            .where(collection.user.email.eq(email))
            .fetch();
    }

    @Override
    public  CollectionDetailDTO getCollectionData(String email, Long id) {
        Tuple collectionData = factory
            .select(collection.id, collection.name)
            .from(collection)
            .where(collection.id.eq(id), collection.user.email.eq(email))
            .fetchOne();

        if (collectionData == null) {
            throw new CollectionNotFoundException("해당 컬렉션을 찾을 수 없습니다.");
        }

        List<BookInfoInCollectionDTO> bookInfo = factory
            .select(
                new QBookInfoInCollectionDTO(bookMeta.title, bookMeta.cover, bookMeta.isbn)
            )
            .from(collection)
            .join(bookMetaCollection).on(collection.id.eq(bookMetaCollection.collection.id))
            .join(bookMeta).on(bookMeta.id.eq(bookMetaCollection.bookMeta.id))
            .where(collection.id.eq(id))
            .where(collection.user.email.eq(email))
            .fetch();

        return new CollectionDetailDTO(collectionData.get(collection.id),
            collectionData.get(collection.name), bookInfo);
    }

    @Override
    public boolean existsBookInCollection(String email, String collectionName, String isbn) {
        return factory.selectOne()
            .from(bookMetaCollection)
            .where(bookMetaCollection.user.email.eq(email)
                .and(bookMetaCollection.collection.name.eq(collectionName))
                .and(bookMetaCollection.bookMeta.isbn.eq(isbn))).fetchFirst() != null;
    }

}
