package com.gomdolbook.api.domain.models.readingLog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long>, ReadingLogRepositoryCustom {

    @Query("select r from ReadingLog r join fetch r.book b where b.isbn = :isbn and r.user.email = :email")
    Optional<ReadingLog> findByEmail(String isbn, String email);
}
