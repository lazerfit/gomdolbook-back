package com.gomdolbook.api.api.dto.book;

import java.util.LinkedHashMap;
import java.util.List;

public record BookCollectionCoverListResponseDTO
    (String name, BookCoverDTO books) {

    public static List<BookCollectionCoverListResponseDTO> from(List<BookCollectionCoverDTO> dtoList) {
        LinkedHashMap<String, BookCoverDTO> tempMap = new LinkedHashMap<>();
        for (BookCollectionCoverDTO dto : dtoList) {
            tempMap
                .computeIfAbsent(dto.name(), k -> new BookCoverDTO())
                .addCovers(dto.cover());
        }

        return tempMap.entrySet().stream()
            .map(e -> new BookCollectionCoverListResponseDTO(e.getKey(),e.getValue())).toList();
    }
}
