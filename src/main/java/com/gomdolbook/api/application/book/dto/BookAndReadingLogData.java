package com.gomdolbook.api.application.book.dto;

import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class BookAndReadingLogData {

    private final String title;
    private final String author;
    private final String pubDate;
    private final String cover;
    private final String publisher;
    private final Status status;
    private final String note1;
    private final String note2;
    private final String note3;
    private final Integer rating;

    @QueryProjection
    public BookAndReadingLogData(Book book) {
        title = book.getBookMeta().getTitle();
        author = book.getBookMeta().getAuthor();
        pubDate = book.getBookMeta().getPubDate();
        cover = book.getBookMeta().getCover();
        publisher = book.getBookMeta().getPublisher();
        status = book.getReadingLog().getStatus();
        note1 = book.getReadingLog().getNote1();
        note2 = book.getReadingLog().getNote2();
        note3 = book.getReadingLog().getNote3();
        rating = book.getReadingLog().getRating();
    }

}
