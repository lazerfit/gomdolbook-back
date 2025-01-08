package com.gomdolbook.api.controllers;

import com.gomdolbook.api.dto.BookDTO;
import com.gomdolbook.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.dto.ReadingLogDTO;
import com.gomdolbook.api.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    @GetMapping("/v1/readingLog/{isbn}")
    public ReadingLogDTO getBook(@PathVariable String isbn) {
        return bookService.getReadingLog(isbn);
    }

    @GetMapping("/v1/book/{isbn}")
    public Mono<BookDTO> fetchItemFromAladin(@PathVariable String isbn) {
        return bookService.fetchItemFromAladin(isbn);
    }

    @PostMapping("/v1/book/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveBook(@Validated @RequestBody BookSaveRequestDTO saveRequest) {
        bookService.saveBook(saveRequest);
    }
}
