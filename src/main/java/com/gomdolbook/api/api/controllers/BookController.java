package com.gomdolbook.api.api.controllers;

import com.gomdolbook.api.api.dto.APIResponseDTO;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.BookSearchResponseDTO;
import com.gomdolbook.api.api.dto.BookAndReadingLogDTO;
import com.gomdolbook.api.service.BookService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @GetMapping("/v1/readingLog/{isbn}")
    public ResponseEntity<APIResponseDTO<BookAndReadingLogDTO>> getBook(@PathVariable String isbn) {
        BookAndReadingLogDTO readingLog = bookService.getReadingLog(isbn);
        APIResponseDTO<BookAndReadingLogDTO> dto = new APIResponseDTO<>(
            readingLog);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/v1/status/{isbn}")
    public ResponseEntity<APIResponseDTO<String>> getStatus(@PathVariable String isbn) {
        String status = bookService.getStatus(isbn);
        APIResponseDTO<String> responseDTO = new APIResponseDTO<>(status);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveBook(@Validated @RequestBody BookSaveRequestDTO saveRequest,
        @RequestParam(name = "email") String email) {

        bookService.saveBook(saveRequest, email);
    }

    @GetMapping("/test/{email}")
    public String test(@PathVariable String email) {
        return bookService.test(email);
    }

//    @GetMapping("/v1/readingLog/get")
//    public ReadingLog getReadingLog(@RequestParam(name = "email") String email,
//        @RequestParam(name = "isbn") String isbn) {
//        return bookService.getReadingLogV2(email, isbn);
//    }
}
