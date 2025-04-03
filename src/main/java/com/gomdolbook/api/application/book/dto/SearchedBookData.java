package com.gomdolbook.api.application.book.dto;

import com.gomdolbook.api.application.book.dto.AladinResponseData.Item;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchedBookData(
    String title,
    String isbn13,
    String cover,
    String author,
    String publisher,
    String pubDate,
    String description
) {

    public static SearchedBookData from(Item item) {
        return new SearchedBookData(
            item.title(),
            item.isbn13(),
            item.cover(),
            item.author(),
            item.publisher(),
            item.pubDate(),
            item.description()
        );
    }

    public static List<SearchedBookData> from(AladinResponseData aladinResponseData) {
        return aladinResponseData.item().stream().map(SearchedBookData::from).toList();
    }
}
