package com.gomdolbook.api.dto;

import com.gomdolbook.api.models.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BookDTO {

    private final String title;

    private final String author;

    private final String pubDate;

    private final String description;

    private final String isbn13;

    private final String cover;

    private final String categoryName;

    private final String publisher;

    @Builder
    public BookDTO(String title, String author, String pubDate, String description,
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

    public BookDTO(AladinAPI aladinAPI) {
        title = aladinAPI.item().getFirst().title();
        author = aladinAPI.item().getFirst().author();
        pubDate = aladinAPI.item().getFirst().pubDate();
        description = aladinAPI.item().getFirst().description();
        isbn13 = aladinAPI.item().getFirst().isbn13();
        cover = aladinAPI.item().getFirst().cover();
        categoryName = aladinAPI.item().getFirst().categoryName();
        publisher = aladinAPI.item().getFirst().publisher();
    }

    public BookDTO(Book book) {
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
