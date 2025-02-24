package com.gomdolbook.api.api.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BookListResponseDTO(
    String cover,
    String title,
    String isbn,
    boolean isReadingLogExists
) {
    @QueryProjection
    public BookListResponseDTO {
    }
}
