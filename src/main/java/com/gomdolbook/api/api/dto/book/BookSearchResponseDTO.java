package com.gomdolbook.api.api.dto.book;

import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import java.util.List;
import lombok.Builder;

@Builder
public record BookSearchResponseDTO(
    String title,
    String isbn13,
    String cover,
    String author,
    String publisher,
    String pubDate,
    String description
) {

    public static BookSearchResponseDTO from(Item item) {
        return new BookSearchResponseDTO(
            item.title(),
            item.isbn13(),
            item.cover(),
            item.author(),
            item.publisher(),
            item.pubDate(),
            item.description()
        );
    }

    public static List<BookSearchResponseDTO> from(AladinAPI aladinAPI) {
        return aladinAPI.item().stream().map(BookSearchResponseDTO::from).toList();
    }
}
