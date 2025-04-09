package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.common.config.annotations.DomainRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@DomainRepository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    Optional<Book> findByIsbn(String isbn);
}
