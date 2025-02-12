package com.gomdolbook.api.api.dto;

import com.gomdolbook.api.persistence.entity.Book;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CollectionBookCoverResponseDTO {
    private final String cover;

    @QueryProjection
    public CollectionBookCoverResponseDTO(String cover) {
        this.cover = cover;
    }

    public CollectionBookCoverResponseDTO(Book book) {
        this.cover = book.getCover();
    }
}
