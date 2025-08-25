package com.gomdolbook.api.application.collection.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BookInfoInCollectionDTO(String title, String cover, String isbn) {

    @QueryProjection
    public BookInfoInCollectionDTO {}
}
