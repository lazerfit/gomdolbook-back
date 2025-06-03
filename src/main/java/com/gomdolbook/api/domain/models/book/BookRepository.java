package com.gomdolbook.api.domain.models.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    @Query("select b from Book b join fetch BookMeta bm on b.bookMeta = bm where b.bookMeta.isbn = :isbn")
    Optional<Book> findByIsbn(String isbn);
}
