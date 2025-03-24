package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.book.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import java.util.List;
import java.util.Optional;


public interface BookRepositoryCustom {

    Optional<BookAndReadingLogDTO> findByUserEmailAndIsbn(String email, String isbn);
    List<BookListResponseDTO> findByReadingStatus(Status status, String email);
    Optional<Status> getStatus(String isbn, String email);
}
