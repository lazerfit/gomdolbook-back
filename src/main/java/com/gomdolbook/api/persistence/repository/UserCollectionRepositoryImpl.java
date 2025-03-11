package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCollectionRepositoryImpl implements UserCollectionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CollectionListResponseDTO> findByEmail(String email) {
//        List<UserCollection> collection = queryFactory.selectFrom(userCollection)
//            .where(userCollection.user.email.eq(email))
//            .fetch();
//
//        return collection.stream()
//            .map(c -> new CollectionListResponseDTO(c.getName(), c.getBooks())).toList();
        return List.of();
    }

}
