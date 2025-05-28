package com.gomdolbook.api.domain.models.bookmetacollection;

import com.gomdolbook.api.domain.models.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookMetaCollectionRepository extends JpaRepository<BookMetaCollection, Long>, BookMetaCollectionRepositoryCustom {

    @Query("select b from BookMetaCollection b where b.user = :user")
    List<BookMetaCollection> findByUser(User user);
}
