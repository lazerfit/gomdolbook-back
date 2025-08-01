package com.gomdolbook.api.domain.models.collection;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("select uc from Collection uc where uc.name = :name and uc.user.email = :email")
    Optional<Collection> find(String name, String email);

    @Query("select c from Collection c where c.id = :id and c.user.email = :email")
    Optional<Collection> findById(Long id, String email);
}
