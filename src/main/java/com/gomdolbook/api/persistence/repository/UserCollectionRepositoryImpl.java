package com.gomdolbook.api.persistence.repository;

import static com.gomdolbook.api.persistence.entity.QUserCollection.userCollection;

import com.gomdolbook.api.persistence.entity.UserCollection;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCollectionRepositoryImpl implements UserCollectionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public UserCollection findByNameAndEmail(String name, String email) {
        return queryFactory.selectFrom(userCollection)
            .where(userCollection.name.eq(name))
            .where(userCollection.user.email.eq(email))
            .fetchOne();
    }
}
