package com.gomdolbook.api.domain.models.collection;

import com.gomdolbook.api.common.config.annotations.DomainRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@DomainRepository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("select uc from Collection uc where uc.name = :name and uc.user.email = :email")
    Optional<Collection> find(String name, String email);
}
