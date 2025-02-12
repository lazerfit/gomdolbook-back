package com.gomdolbook.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Long id;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private String pubDate;

    @Column
    private String description;

    @Column
    private String isbn13;

    @Column
    private String cover;

    @Column
    private String categoryName;

    @Column
    private String publisher;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "READINGLOG_ID")
    private ReadingLog readingLog;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private final List<BookUserCollection> bookUserCollections = new ArrayList<>();

    @Builder
    public Book(String title, String author, String pubDate, String description, String isbn13,
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

}
