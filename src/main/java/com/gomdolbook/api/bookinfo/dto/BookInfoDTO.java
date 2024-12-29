package com.gomdolbook.api.bookinfo.dto;

import com.gomdolbook.api.bookinfo.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BookInfoDTO {

    private final String title;

    private final String author;

    private final String pubDate;

    private final String description;

    private final String isbn13;

    private final String cover;

    private final String categoryName;

    private final String publisher;

    @Builder
    public BookInfoDTO(String title, String author, String pubDate, String description,
        String isbn13,
        String cover, String categoryName, String publisher) {
        this.title = title;
        this.author = author;
        this.pubDate = pubDate;
        this.description = description;
        this.isbn13 = isbn13;
        this.cover = cover;
        this.categoryName = categoryName;
        this.publisher = publisher;
    }

    public BookInfoDTO(AladinAPI aladinAPI) {
        title = aladinAPI.items().getFirst().title();
        author = aladinAPI.items().getFirst().author();
        pubDate = aladinAPI.items().getFirst().pubDate();
        description = aladinAPI.items().getFirst().description();
        isbn13 = aladinAPI.items().getFirst().isbn13();
        cover = aladinAPI.items().getFirst().cover();
        categoryName = aladinAPI.items().getFirst().categoryName();
        publisher = aladinAPI.items().getFirst().publisher();
    }

    public BookInfoDTO(Book book) {
        title = book.getTitle();
        author = book.getAuthor();
        pubDate = book.getPubDate();
        description = book.getDescription();
        isbn13 = book.getIsbn13();
        cover = book.getCover();
        categoryName = book.getCategoryName();
        publisher = book.getPublisher();
    }
}
