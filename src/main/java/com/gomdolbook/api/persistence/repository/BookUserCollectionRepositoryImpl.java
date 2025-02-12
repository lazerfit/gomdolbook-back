package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QBookUserCollection.bookUserCollection;

import com.gomdolbook.api.api.dto.BookUserCollectionCoverResponseDTO;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookUserCollectionRepositoryImpl implements BookUserCollectionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public BookUserCollectionCoverResponseDTO getBookCovers(String collectionName, String email) {
        List<Tuple> bookCoverList = queryFactory.select(bookUserCollection.userCollection.name,
                bookUserCollection.book.cover).from(bookUserCollection)
            .where(bookUserCollection.userCollection.name.eq(collectionName))
            .where(bookUserCollection.user.email.eq(email))
            .limit(4)
            .fetch();

        if (bookCoverList.isEmpty()) {
            return new BookUserCollectionCoverResponseDTO(collectionName);
        }

        var dto = new BookUserCollectionCoverResponseDTO(
            bookCoverList.getFirst().get(bookUserCollection.userCollection.name));

        for (Tuple tuple : bookCoverList) {
            dto.addBookCover(tuple.get(bookUserCollection.book.cover));
        }

        return dto;
    }
}
