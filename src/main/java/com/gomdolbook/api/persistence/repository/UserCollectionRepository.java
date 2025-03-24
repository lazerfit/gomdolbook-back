package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.UserCollection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserCollectionRepository extends JpaRepository<UserCollection, Long>,
    UserCollectionRepositoryCustom {

    @Query("select uc from UserCollection uc where uc.name = :name and uc.user.email = :email")
    Optional<UserCollection> findByName(String name, String email);
}
