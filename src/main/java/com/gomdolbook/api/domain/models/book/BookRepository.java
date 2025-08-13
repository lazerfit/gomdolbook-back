package com.gomdolbook.api.domain.models.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    @Query("select b from Book b join fetch BookMeta bm on b.bookMeta = bm join User u on b.user.id = u.id where b.bookMeta.isbn = :isbn and b.user.email = :email")
    Optional<Book> findByIsbn(String isbn, String email);
}
