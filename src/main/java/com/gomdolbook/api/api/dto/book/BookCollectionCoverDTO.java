package com.gomdolbook.api.api.dto.book;

import com.querydsl.core.annotations.QueryProjection;

public record BookCollectionCoverDTO(
    String name,
    String cover
) {

    @QueryProjection
    public BookCollectionCoverDTO {
    }
}
