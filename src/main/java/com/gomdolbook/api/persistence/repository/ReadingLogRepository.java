package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.ReadingLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

    @Query("select r from ReadingLog r join r.book b where b.isbn13 = :isbn and r.user.email = :email")
    Optional<ReadingLog> findByUserEmail(String isbn, String email);
}
