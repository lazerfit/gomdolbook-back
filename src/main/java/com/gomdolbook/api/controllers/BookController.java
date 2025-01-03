package com.gomdolbook.api.controllers;

import com.gomdolbook.api.dto.BookDTO;
import com.gomdolbook.api.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    @GetMapping("/v1/book/{isbn}")
    public Mono<BookDTO> getBook(@PathVariable String isbn) {
        return bookService.getBook(isbn);
    }

}
