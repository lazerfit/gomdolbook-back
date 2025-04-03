package com.gomdolbook.api.domain.models.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    @Query("select b from Book b join fetch b.bookCollections where b.isbn13 = :isbn")
    Optional<Book> find(String isbn);
}
