package com.gomdolbook.api.api.dto;

import com.gomdolbook.api.persistence.entity.Book;
import com.querydsl.core.annotations.QueryProjection;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CollectionListResponseDTO {

    private final String name;
    private final List<String> booksCover = new ArrayList<>();

    @QueryProjection
    public CollectionListResponseDTO(String name, List<Book> books) {
        this.name = name;
        books.forEach(this::addBookCover);
    }

    public void addBookCover(Book book) {
        if (!booksCover.contains(book.getCover())) {
            booksCover.add(book.getCover());
        }
    }
}
