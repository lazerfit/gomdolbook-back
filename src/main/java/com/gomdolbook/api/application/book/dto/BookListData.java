package com.gomdolbook.api.application.book.dto;

import com.gomdolbook.api.domain.models.readinglog.ReadingLog.Status;
import com.querydsl.core.annotations.QueryProjection;

public record BookListData(
    String cover,
    String title,
    String isbn,
    Status status
) {
    @QueryProjection
    public BookListData {
    }
}
