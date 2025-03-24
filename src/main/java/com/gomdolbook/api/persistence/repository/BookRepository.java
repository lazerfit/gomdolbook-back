package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    @Query("select b from Book b join fetch b.bookUserCollections where b.isbn13 = :isbn")
    Optional<Book> findByIsbn(String isbn);
}
