package com.gomdolbook.api.domain.models.bookmeta;

import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.domain.models.book.Book;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class BookMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_META_ID")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String pubDate;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String cover;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private String publisher;

    @OneToMany(mappedBy = "bookMeta")
    private final List<Book> books = new ArrayList<>();

    @Builder
    public BookMeta(String title, String author, String pubDate, String description, String isbn,
        String cover, String categoryName, String publisher) {
        this.title = title;
        this.author = author;
        this.pubDate = pubDate;
        this.description = description;
        this.isbn = isbn;
        this.cover = cover;
        this.categoryName = categoryName;
        this.publisher = publisher;
    }

    public static BookMeta of(BookSaveCommand command) {
        return BookMeta.builder()
            .title(command.title())
            .author(command.author())
            .pubDate(command.pubDate())
            .description(command.description())
            .isbn(command.isbn())
            .cover(command.cover())
            .categoryName(command.categoryName())
            .publisher(command.publisher())
            .build();
    }
}
