package com.gomdolbook.api.api.dto;

import lombok.Builder;

@Builder
public record BookSearchResponseDTO(
    String title,
    String isbn13,
    String cover,
    String author,
    String publisher,
    String pubDate,
    String description
) {

}
