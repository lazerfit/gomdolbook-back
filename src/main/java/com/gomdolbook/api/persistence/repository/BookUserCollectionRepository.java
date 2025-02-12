package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.BookUserCollection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookUserCollectionRepository extends JpaRepository<BookUserCollection, Long> {

    @Query("select bu from BookUserCollection bu where bu.userCollection.name = :name")
    List<BookUserCollection> findByUserCollectionName(String name);

    @Query("select bu from BookUserCollection bu where bu.user.email = :email")
    List<BookUserCollection> findByUserEmail(String email);
}
