package com.gomdolbook.api.domain.models.collection;

import static com.gomdolbook.api.domain.models.bookmeta.QBookMeta.bookMeta;
import static com.gomdolbook.api.domain.models.bookmetacollection.QBookMetaCollection.bookMetaCollection;
import static com.gomdolbook.api.domain.models.collection.QCollection.collection;
import static com.gomdolbook.api.domain.models.user.QUser.user;

import com.gomdolbook.api.application.collection.dto.BookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.QBookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.QBookInfoInCollectionDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CollectionRepositoryImpl implements CollectionRepositoryCustom{

    private final JPAQueryFactory factory;

    @Override
    public List<BookCoverDataInCollectionDTO> findCollections(String email) {
        return factory
            .select(
                new QBookCoverDataInCollectionDTO(
                    collection.id,
                    collection.name,
                    bookMeta.cover
                )
            )
            .from(collection)
            .leftJoin(collection.bookMetaCollections, bookMetaCollection)
            .leftJoin(bookMetaCollection.bookMeta, bookMeta)
            .where(collection.user.email.eq(email))
            .fetch();
    }

    @Override
    public List<BookInfoInCollectionDTO> findCollection(String email, Long id) {
        return factory
            .select(
                new QBookInfoInCollectionDTO(bookMeta.title, bookMeta.cover, bookMeta.isbn)
            )
            .from(collection)
            .join(collection.bookMetaCollections, bookMetaCollection)
            .join(bookMetaCollection.bookMeta, bookMeta)
            .join(collection.user, user)
            .where(collection.id.eq(id))
            .where(collection.user.email.eq(email))
            .fetch();
    }
}
