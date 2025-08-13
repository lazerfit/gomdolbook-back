package com.gomdolbook.api.domain.models.readinglog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long>, ReadingLogRepositoryCustom {

    @Query("select r from ReadingLog r join Book b on r.book.id = b.id where r.id = :id and b.user.email = :email")
    Optional<ReadingLog> findByIdAndEmail(Long id, String email);
}
