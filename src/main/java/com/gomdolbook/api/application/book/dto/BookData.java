package com.gomdolbook.api.application.book.dto;

import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import com.gomdolbook.api.domain.models.book.Book;

public record BookData(
    String title,
    String author,
    String pubDate,
    String description,
    String isbn13,
    String cover,
    String categoryName,
    String publisher
) {

    public static BookData from(Book book) {
        return new BookData(
            book.getTitle(),
            book.getAuthor(),
            book.getPubDate(),
            book.getDescription(),
            book.getIsbn13(),
            book.getCover(),
            book.getCategoryName(),
            book.getPublisher()
        );
    }

    public static BookData from(AladinResponseData aladinResponseData) {
        Item item = aladinResponseData.item().getFirst();
        return new BookData(
            item.title(),
            item.author(),
            item.pubDate(),
            item.description(),
            item.isbn13(),
            item.cover(),
            item.categoryName(),
            item.publisher()
        );
    }
}
