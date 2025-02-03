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
}
