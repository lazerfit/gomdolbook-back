package com.gomdolbook.api.domain.models.collection;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionRepository extends JpaRepository<Collection, Long>, CollectionRepositoryCustom {

    @Query("select c from Collection c where c.id = :id and c.user.email = :email")
    Optional<Collection> findByIdAndEmail(Long id, String email);

    @Query("select c from Collection c join fetch User u on c.user.id = u.id where c.id = :id")
    Optional<Collection> findByIdWithUser(Long id);
}
