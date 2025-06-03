package com.gomdolbook.api.application.book.dto;

import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;

public record BookData(
    String title,
    String author,
    String pubDate,
    String description,
    String isbn,
    String cover,
    String categoryName,
    String publisher
) {

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
