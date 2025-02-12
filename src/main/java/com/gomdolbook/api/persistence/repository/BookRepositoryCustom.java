package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.LibraryResponseDTO;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepositoryCustom {

    Optional<BookAndReadingLogDTO> findByUserEmailAndIsbn(String email, String isbn);
    List<LibraryResponseDTO> findByReadingStatus(Status status, String email);
}
