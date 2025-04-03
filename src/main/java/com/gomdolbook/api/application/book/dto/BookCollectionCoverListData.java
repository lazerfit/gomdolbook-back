package com.gomdolbook.api.application.book.dto;

import java.util.LinkedHashMap;
import java.util.List;

public record BookCollectionCoverListData
    (String name, BookCoverData books) {

    public static List<BookCollectionCoverListData> from(List<BookCollectionCoverData> dtoList) {
        LinkedHashMap<String, BookCoverData> tempMap = new LinkedHashMap<>();
        for (BookCollectionCoverData dto : dtoList) {
            tempMap
                .computeIfAbsent(dto.name(), k -> new BookCoverData())
                .addCovers(dto.cover());
        }

        return tempMap.entrySet().stream()
            .map(e -> new BookCollectionCoverListData(e.getKey(),e.getValue())).toList();
    }
}
