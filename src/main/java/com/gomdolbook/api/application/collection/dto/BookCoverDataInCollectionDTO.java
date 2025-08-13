package com.gomdolbook.api.application.collection.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BookCoverDataInCollectionDTO(
    Long id,
    String name,
    String cover
) {

    @QueryProjection
    public BookCoverDataInCollectionDTO {
    }
}
