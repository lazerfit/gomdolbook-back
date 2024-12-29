package com.gomdolbook.api.bookinfo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BookInfoReactiveRepository extends ReactiveCrudRepository<Book,Long> {

    Mono<Book> findByIsbn13(String isbn13);
}
