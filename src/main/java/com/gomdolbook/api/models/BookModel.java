package com.gomdolbook.api.models;

import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.api.dto.AladinAPI.Item;
import com.gomdolbook.api.api.dto.BookDTO;
import com.gomdolbook.api.api.dto.BookResponseDTO;
import com.gomdolbook.api.api.dto.BookSearchResponseDTO;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import java.util.List;

public class BookModel {

    private BookModel(){}

    public static BookDTO toBookDTO(Book book) {
        return BookDTO.builder()
            .title(book.getTitle())
            .author(book.getAuthor())
            .pubDate(book.getPubDate())
            .description(book.getDescription())
            .isbn13(book.getIsbn13())
            .cover(book.getCover())
            .categoryName(book.getCategoryName())
            .publisher(book.getPublisher())
            .build();
    }

    public static BookDTO toBookDTO(AladinAPI aladinAPI) {
        Item item = aladinAPI.item().getFirst();
        return BookDTO.builder()
            .title(item.title())
            .author(item.author())
            .pubDate(item.pubDate())
            .description(item.description())
            .isbn13(item.isbn13())
            .cover(item.cover())
            .categoryName(item.categoryName())
            .publisher(item.publisher())
            .build();
    }

    public static BookDTO toBookDTO(Item item) {
        return BookDTO.builder()
            .title(item.title())
            .author(item.author())
            .pubDate(item.pubDate())
            .description(item.description())
            .isbn13(item.isbn13())
            .cover(item.cover())
            .categoryName(item.categoryName())
            .publisher(item.publisher())
            .build();
    }

    public static BookSearchResponseDTO toBookSearchResponseDTO(Item item) {
        return BookSearchResponseDTO.builder()
            .title(item.title())
            .author(item.author())
            .pubDate(item.pubDate())
            .description(item.description())
            .isbn13(item.isbn13())
            .cover(item.cover())
            .publisher(item.publisher())
            .build();
    }

    public static List<BookDTO> convertListBookDTO(AladinAPI aladinAPI) {
        return aladinAPI.item().stream().map(BookModel::toBookDTO).toList();
    }

    public static List<BookSearchResponseDTO> toListBookSearchResponseDTO(AladinAPI aladinAPI) {
        return aladinAPI.item().stream().map(BookModel::toBookSearchResponseDTO).toList();
    }

    public static BookResponseDTO convertBookResponseDTO(AladinAPI aladinAPI, Status status) {
        Item item = aladinAPI.item().getFirst();
        return BookResponseDTO.builder()
            .title(item.title())
            .author(item.author())
            .pubDate(item.pubDate())
            .description(item.description())
            .isbn13(item.isbn13())
            .cover(item.cover())
            .categoryName(item.categoryName())
            .publisher(item.publisher())
            .status(status)
            .build();
    }
}
