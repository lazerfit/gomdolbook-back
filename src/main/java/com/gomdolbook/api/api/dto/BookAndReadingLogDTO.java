package com.gomdolbook.api.api.dto;

import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class BookAndReadingLogDTO {

    private final String title;

    private final String author;

    private final String pubDate;

    private final String cover;

    private final String publisher;

    private final Status status;

    private final String note1;

    private final String note2;

    private final String note3;

    @QueryProjection
    public BookAndReadingLogDTO(Book book) {
        title = book.getTitle();
        author = book.getAuthor();
        pubDate = book.getPubDate();
        cover = book.getCover();
        publisher = book.getPublisher();
        status = book.getReadingLog().getStatus();
        note1 = book.getReadingLog().getNote1();
        note2 = book.getReadingLog().getNote2();
        note3 = book.getReadingLog().getNote3();
    }

    public BookAndReadingLogDTO(String title, String author, String pubDate, String cover,
        String publisher, Status status, String note1, String note2, String note3) {
        this.title = title;
        this.author = author;
        this.pubDate = pubDate;
        this.cover = cover;
        this.publisher = publisher;
        this.status = status;
        this.note1 = note1;
        this.note2 = note2;
        this.note3 = note3;
    }
}
