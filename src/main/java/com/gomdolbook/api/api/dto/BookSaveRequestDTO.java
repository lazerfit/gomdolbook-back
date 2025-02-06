package com.gomdolbook.api.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record BookSaveRequestDTO(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String pubDate,
    @NotBlank String description,
    @NotBlank String isbn13,
    @NotBlank String cover,
    @NotBlank String categoryName,
    @NotBlank String publisher,
    @NotBlank String status,
    @NotBlank String email
) {
}
