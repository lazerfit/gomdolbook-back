package com.gomdolbook.api.api.dto;

import com.querydsl.core.annotations.QueryProjection;

public record LibraryResponseDTO(
    String cover,
    String title,
    String isbn
) {
    @QueryProjection
    public LibraryResponseDTO {
    }
}
