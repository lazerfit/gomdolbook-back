package com.gomdolbook.api.repository.reactive;

import com.gomdolbook.api.models.Book;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BookReactiveRepository extends ReactiveCrudRepository<Book,Long> {

    Mono<Book> findByIsbn13(String isbn13);
}
