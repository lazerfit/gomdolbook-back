package com.gomdolbook.api.application.book.web;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.command.BookSaveHandler;
import com.gomdolbook.api.application.book.command.RatingUpdateCommand;
import com.gomdolbook.api.application.book.command.RatingUpdateHandler;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateCommand;
import com.gomdolbook.api.application.book.command.ReadingLogUpdateHandler;
import com.gomdolbook.api.application.book.command.StatusUpdateCommand;
import com.gomdolbook.api.application.book.command.StatusUpdateHandler;
import com.gomdolbook.api.application.book.dto.BookAndReadingLogData;
import com.gomdolbook.api.application.book.dto.BookData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.application.book.dto.SearchedBookData;
import com.gomdolbook.api.application.book.dto.StatusData;
import com.gomdolbook.api.application.shared.ApiResponse;
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

    private final BookApplicationService bookApplicationService;
    private final BookSaveHandler bookSaveHandler;
    private final RatingUpdateHandler ratingUpdateHandler;
    private final StatusUpdateHandler statusUpdateHandler;
    private final ReadingLogUpdateHandler readingLogUpdateHandler;

    @GetMapping("/v1/readingLog")
    public ResponseEntity<ApiResponse<BookAndReadingLogData>> getReadingLog(
        @RequestParam(name = "isbn") String isbn) {
        BookAndReadingLogData readingLog = bookApplicationService.getReadingLog(isbn);
        ApiResponse<BookAndReadingLogData> dto = new ApiResponse<>(
            readingLog);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/v1/readingLog/update")
    public ResponseEntity<String> updateReadingLog(@RequestBody @Valid ReadingLogUpdateCommand command) {
        readingLogUpdateHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/readingLog/rating/update")
    public ResponseEntity<Void> updateRating(@RequestParam("isbn") String isbn,
        @RequestParam("star") int star) {
        RatingUpdateCommand command = new RatingUpdateCommand(isbn, star);
        ratingUpdateHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/status/{isbn}")
    public ResponseEntity<ApiResponse<StatusData>> getStatus(@PathVariable String isbn) {
        StatusData status = bookApplicationService.getStatus(isbn);
        ApiResponse<StatusData> responseDTO = new ApiResponse<>(status);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/v1/status/{isbn}/update")
    public ResponseEntity<Void> updateStatus(@RequestParam("status") String status, @PathVariable String isbn) {
        StatusUpdateCommand command = new StatusUpdateCommand(isbn, status);
        statusUpdateHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/book/{isbn}")
    public Mono<ApiResponse<BookData>> fetchItemFromAladin(@PathVariable String isbn) {
        return bookApplicationService.fetchItemFromAladin(isbn).map(ApiResponse::new);
    }

    @GetMapping("/v1/book/search")
    public Mono<ApiResponse<List<SearchedBookData>>> searchBook(
        @RequestParam(name = "q") String q) {
        return bookApplicationService.searchBookFromAladin(q).map(ApiResponse::new);
    }

    @PostMapping("/v1/book/save")
    public ResponseEntity<Void> saveBook(@RequestBody @Validated BookSaveCommand command) {
        bookSaveHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/book/Library")
    public ResponseEntity<ApiResponse<List<BookListData>>> getLibrary(
        @RequestParam("status") String status) {
        List<BookListData> libraries = bookApplicationService.getLibrary(status);
        if (libraries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ApiResponse<List<BookListData>> responseDTO = new ApiResponse<>(
            libraries);
        return new ResponseEntity<>(responseDTO,HttpStatus.OK);
    }
}
