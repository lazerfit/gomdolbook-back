package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    Optional<Book> findByIsbn13(String isbn13);
}
