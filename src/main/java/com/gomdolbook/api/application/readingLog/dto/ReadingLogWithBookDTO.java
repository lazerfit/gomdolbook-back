package com.gomdolbook.api.application.readingLog.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record ReadingLogWithBookDTO(
    Long id,
    String title,
    String author,
    String cover,
    String publisher,
    String summary,
    String note,
    Integer rating,
    LocalDateTime startedAt,
    LocalDateTime finishedAt) {

    @QueryProjection
    public ReadingLogWithBookDTO {
    }
}
