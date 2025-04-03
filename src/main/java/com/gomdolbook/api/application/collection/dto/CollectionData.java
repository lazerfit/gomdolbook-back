package com.gomdolbook.api.application.collection.dto;

import com.gomdolbook.api.domain.models.book.Book;
import com.querydsl.core.annotations.QueryProjection;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CollectionData {

    private final String name;
    private final List<String> booksCover = new ArrayList<>();

    @QueryProjection
    public CollectionData(String name, List<Book> books) {
        this.name = name;
        books.forEach(this::addBookCover);
    }

    public void addBookCover(Book book) {
        if (!booksCover.contains(book.getCover())) {
            booksCover.add(book.getCover());
        }
    }
}
