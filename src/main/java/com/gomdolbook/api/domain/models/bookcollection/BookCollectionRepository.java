package com.gomdolbook.api.domain.models.bookcollection;

import com.gomdolbook.api.common.config.annotations.DomainRepository;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

@DomainRepository
public interface BookCollectionRepository extends JpaRepository<BookCollection, Long>,
    BookCollectionRepositoryCustom {

    @Query("select bu from BookCollection bu where bu.user = :user")
    List<BookCollection> find(User user);

    @Query("select bu from BookCollection bu where bu.user = :user and bu.collection = :collection")
    List<BookCollection> find(User user, Collection collection);

    @Query("select case when exists (select 1 from BookCollection buc where buc.book = :book and buc.user = :user and buc.collection = :collection ) then true else false end")
    boolean existsBook(User user, Collection collection, Book book);

    @Modifying
    @Query("delete from BookCollection bu where bu.collection = :userCollection")
    void deleteByUserCollection(Collection collection);
}
