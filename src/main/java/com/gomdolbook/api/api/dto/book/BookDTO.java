package com.gomdolbook.api.api.dto.book;

import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import com.gomdolbook.api.persistence.entity.Book;

public record BookDTO(
    String title,
    String author,
    String pubDate,
    String description,
    String isbn13,
    String cover,
    String categoryName,
    String publisher
) {

    public static BookDTO from(Book book) {
        return new BookDTO(
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

    public static BookDTO from(AladinAPI aladinAPI) {
        Item item = aladinAPI.item().getFirst();
        return new BookDTO(
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
