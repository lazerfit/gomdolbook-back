package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.UserCollection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCollectionRepository extends JpaRepository<UserCollection, Long>,
    UserCollectionRepositoryCustom {
    Optional<UserCollection> findByName(String name);
}
