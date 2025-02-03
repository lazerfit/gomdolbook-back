package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepositoryCustom {

    Optional<BookAndReadingLogDTO> findByUserEmailAndIsbn(String email, String isbn);
}
