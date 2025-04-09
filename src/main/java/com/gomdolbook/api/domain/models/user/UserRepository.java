package com.gomdolbook.api.domain.models.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.email = :email")
    Optional<User> find(String email);
}
