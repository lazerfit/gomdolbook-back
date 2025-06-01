package com.gomdolbook.api.domain.models.readinglog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long>, ReadingLogRepositoryCustom {

    @Query("select r from ReadingLog r join fetch r.book b join fetch b.bookMeta bm where bm.isbn = :isbn and r.user.email = :email")
    Optional<ReadingLog> findByIsbnAndEmail(String isbn, String email);
}
