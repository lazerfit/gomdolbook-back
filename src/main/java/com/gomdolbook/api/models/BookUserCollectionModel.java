package com.gomdolbook.api.models;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.BookCoverDTO;
import java.util.LinkedHashMap;
import java.util.List;

public class BookUserCollectionModel {

    private BookUserCollectionModel() {
    }

    public static List<BookCollectionCoverListResponseDTO> toListResponseDTO(
        List<BookCollectionCoverDTO> dtoList) {
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
