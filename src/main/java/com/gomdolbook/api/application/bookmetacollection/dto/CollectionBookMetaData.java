package com.gomdolbook.api.application.bookmetacollection.dto;

import com.querydsl.core.annotations.QueryProjection;

public record CollectionBookMetaData(
    Long id,
    String cover,
    String title,
    String isbn
) {
    @QueryProjection
    public CollectionBookMetaData{}
}
