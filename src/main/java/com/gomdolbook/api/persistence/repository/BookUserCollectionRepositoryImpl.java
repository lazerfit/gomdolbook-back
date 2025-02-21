package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QBook.book;
import static com.gomdolbook.api.persistence.entity.QBookUserCollection.bookUserCollection;
import static com.gomdolbook.api.persistence.entity.QUserCollection.userCollection;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.LibraryResponseDTO;
import com.gomdolbook.api.api.dto.QBookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.QLibraryResponseDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
    public List<LibraryResponseDTO> getCollection(String name, String email) {

        return queryFactory.select(
                new QLibraryResponseDTO(
                    bookUserCollection.book.cover,
                    bookUserCollection.book.title,
                    bookUserCollection.book.isbn13
                )
            ).from(bookUserCollection)
            .where(bookUserCollection.user.email.eq(email))
            .where(bookUserCollection.userCollection.name.eq(name))
            .fetch();
    }

}
