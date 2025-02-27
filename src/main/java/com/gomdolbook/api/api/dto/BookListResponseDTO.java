package com.gomdolbook.api.api.dto;

import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.querydsl.core.annotations.QueryProjection;

public record BookListResponseDTO(
    String cover,
    String title,
    String isbn,
    Status status
) {
    @QueryProjection
    public BookListResponseDTO {
    }
}
