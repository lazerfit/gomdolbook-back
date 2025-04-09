package com.gomdolbook.api.domain.models.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    Optional<Book> findByIsbn(String isbn);
}
