package com.gomdolbook.api.domain.models.bookmetacollection;

import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookMetaCollectionRepository extends JpaRepository<BookMetaCollection, Long>, BookMetaCollectionRepositoryCustom {

    @Query("select b from BookMetaCollection b where b.user = :user")
    List<BookMetaCollection> findByUser(User user);

    @Query("select case when exists (select 1 from BookMetaCollection bmc where bmc.bookMeta = :bookMeta and bmc.user = :user and bmc.collection = :collection ) then true else false end")
    boolean isExistsByBookMetaAndCollection(User user, Collection collection, BookMeta bookMeta);

    Optional<BookMetaCollection> findByUserAndBookMetaAndCollection(User user,BookMeta bookMeta, Collection collection);
}
