package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.UserCollection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookUserCollectionRepository extends JpaRepository<BookUserCollection, Long>, BookUserCollectionRepositoryCustom {

    @Query("select bu from BookUserCollection bu where bu.user = :user")
    List<BookUserCollection> findByUser(User user);

    @Query("select bu from BookUserCollection bu where bu.user = :user and bu.userCollection = :collection")
    List<BookUserCollection> findByUserAndUserCollection(User user, UserCollection collection);

    @Query("select case when exists (select 1 from BookUserCollection buc where buc.book = :book and buc.user = :user and buc.userCollection = :collection ) then true else false end")
    boolean existsBook(User user, UserCollection collection, Book book);

    @Modifying
    @Query("delete from BookUserCollection bu where bu.userCollection = :userCollection")
    void deleteByUserCollection(UserCollection userCollection);
}
