package com.gomdolbook.api.api.controllers;

import com.gomdolbook.api.api.dto.APIResponseDTO;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.ReadingLogDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<APIResponseDTO<ReadingLogDTO>> getBook(@PathVariable String isbn) {
        ReadingLogDTO readingLog = bookService.getReadingLog(isbn);
        APIResponseDTO<ReadingLogDTO> dto = new APIResponseDTO<>(
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
    public Mono<APIResponseDTO<List<BookDTO>>> searchBook(@RequestParam(name = "q") String q) {
        return bookService.searchBookFromAladin(q).map(APIResponseDTO::new);
    }

    @PostMapping("/v1/book/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveBook(@Validated @RequestBody BookSaveRequestDTO saveRequest) {
        bookService.saveBook(saveRequest);
    }
}
