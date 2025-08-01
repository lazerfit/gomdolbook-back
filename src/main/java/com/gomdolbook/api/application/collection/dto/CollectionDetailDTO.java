package com.gomdolbook.api.application.collection.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;

public record CollectionDetailDTO(
    Long id,
    String collectionName,
    List<BookInfoInCollectionDTO> books
) {
    @QueryProjection
    public CollectionDetailDTO {}
}
