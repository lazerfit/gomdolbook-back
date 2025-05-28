package com.gomdolbook.api.application.bookmetacollection.dto;

import com.querydsl.core.annotations.QueryProjection;

public record CollectionBookMetaData(
    String cover,
    String title,
    String isbn
) {
    @QueryProjection
    public CollectionBookMetaData{
        //QueryDSL projection을 위한 생성자
    }
}
