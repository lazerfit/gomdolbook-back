package com.gomdolbook.api.bookinfo;

import com.gomdolbook.api.bookinfo.dto.BookInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class BookInfoController {

    private final BookInfoService bookInfoService;

    @GetMapping("/book/{isbn}")
    public Mono<BookInfoDTO> getBookInfo(@PathVariable String isbn) {
        return bookInfoService.getBookInfo(isbn);
    }
}
