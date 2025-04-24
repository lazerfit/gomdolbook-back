package com.gomdolbook.api.application.book.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class FinishedBookCalendarData {

    private String isbn;
    private String cover;
    private Integer rating;
    private LocalDate finishedAt;

    @QueryProjection
    public FinishedBookCalendarData(String isbn, String cover, Integer rating,
        LocalDateTime finishedAt) {
        this.isbn = isbn;
        this.cover = cover;
        this.rating = rating;
        this.finishedAt = finishedAt != null ? finishedAt.toLocalDate() : null;
    }
}
