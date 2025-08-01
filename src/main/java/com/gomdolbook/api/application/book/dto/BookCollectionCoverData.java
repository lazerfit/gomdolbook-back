package com.gomdolbook.api.application.book.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BookCollectionCoverData(
    Long id,
    String name,
    String cover
) {

    @QueryProjection
    public BookCollectionCoverData {
    }
}
