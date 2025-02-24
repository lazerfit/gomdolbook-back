package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookUserCollectionRepository extends JpaRepository<BookUserCollection, Long>, BookUserCollectionRepositoryCustom {

    @Query("select bu from BookUserCollection bu where bu.userCollection.name = :name")
    List<BookUserCollection> findByUserCollectionName(String name);

    @Query("select bu from BookUserCollection bu where bu.user.email = :email")
    List<BookUserCollection> findByUserEmail(String email);

    @Query("select case when count(buc) > 0 then true else false end from BookUserCollection buc where buc.book = :book and buc.user = :user")
    boolean existsBookAndUser(Book book, User user);
}
