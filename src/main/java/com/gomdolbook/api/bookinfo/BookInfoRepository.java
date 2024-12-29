package com.gomdolbook.api.bookinfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BookInfoRepository extends JpaRepository<Book, Long> {
    Mono<Book> findByIsbn13(String isbn13);
}
