package com.gomdolbook.api.api.controllers;

import com.gomdolbook.api.api.dto.APIResponseDTO;
import com.gomdolbook.api.api.dto.ReadingLogUpdateRequestDTO;
import com.gomdolbook.api.api.dto.StatusDTO;
import com.gomdolbook.api.api.dto.book.BookAndReadingLogDTO;
import com.gomdolbook.api.api.dto.book.BookDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.book.BookSearchResponseDTO;
import com.gomdolbook.api.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @GetMapping("/v1/readingLog")
    public ResponseEntity<APIResponseDTO<BookAndReadingLogDTO>> getReadingLog(
        @RequestParam(name = "isbn") String isbn) {
        BookAndReadingLogDTO readingLog = bookService.getReadingLog(isbn);
        APIResponseDTO<BookAndReadingLogDTO> dto = new APIResponseDTO<>(
            readingLog);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/v1/readingLog/update")
    public ResponseEntity<String> updateReadingLog(@RequestBody @Valid ReadingLogUpdateRequestDTO request) {
        bookService.updateReadingLog(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/readingLog/rating/update")
    public ResponseEntity<Void> updateRating(@RequestParam("isbn") String isbn,
        @RequestParam("star") int star) {
        bookService.updateRating(star, isbn);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/status/{isbn}")
    public ResponseEntity<APIResponseDTO<StatusDTO>> getStatus(@PathVariable String isbn) {
        StatusDTO status = bookService.getStatus(isbn);
        APIResponseDTO<StatusDTO> responseDTO = new APIResponseDTO<>(status);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/v1/status/{isbn}/update")
    public ResponseEntity<Void> updateStatus(@RequestParam("status") String status, @PathVariable String isbn) {
        bookService.updateStatus(isbn, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/book/{isbn}")
    public Mono<APIResponseDTO<BookDTO>> fetchItemFromAladin(@PathVariable String isbn) {
        return bookService.fetchItemFromAladin(isbn).map(APIResponseDTO::new);
    }

    @GetMapping("/v1/book/search")
    public Mono<APIResponseDTO<List<BookSearchResponseDTO>>> searchBook(
        @RequestParam(name = "q") String q) {
        return bookService.searchBookFromAladin(q).map(APIResponseDTO::new);
    }

    @PostMapping("/v1/book/save")
    public ResponseEntity<Void> saveBook(@RequestBody @Validated BookSaveRequestDTO saveRequest) {
        bookService.saveBook(saveRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/book/Library")
    public ResponseEntity<APIResponseDTO<List<BookListResponseDTO>>> getLibrary(
        @RequestParam("status") String status) {
        List<BookListResponseDTO> libraries = bookService.getLibrary(status);
        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        APIResponseDTO<List<BookListResponseDTO>> responseDTO = new APIResponseDTO<>(
            libraries);
        return new ResponseEntity<>(responseDTO,HttpStatus.OK);
    }
}
