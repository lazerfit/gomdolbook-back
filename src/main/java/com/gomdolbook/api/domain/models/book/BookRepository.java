package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.common.config.annotations.DomainRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@DomainRepository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    @Query("select b from Book b join fetch BookMeta bm on b.bookMeta = bm where b.bookMeta.isbn = :isbn")
    Optional<Book> findByIsbn(String isbn);
}
