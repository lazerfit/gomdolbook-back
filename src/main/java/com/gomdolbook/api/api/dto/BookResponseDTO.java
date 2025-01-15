package com.gomdolbook.api.api.dto;

import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import lombok.Builder;

@Builder
public record BookResponseDTO(
    String title,
    String author,
    String pubDate,
    String description,
    String isbn13,
    String cover,
    String categoryName,
    String publisher,
    Status status
) {

}
