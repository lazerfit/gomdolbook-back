package com.gomdolbook.api.repository.jpa;

import com.gomdolbook.api.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findByIsbn13(String isbn13);
}
