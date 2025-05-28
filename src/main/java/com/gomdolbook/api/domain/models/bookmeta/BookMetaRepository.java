package com.gomdolbook.api.domain.models.bookmeta;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMetaRepository extends JpaRepository<BookMeta, Long> {
    Optional<BookMeta> findByIsbn(String isbn);
}
