package com.gomdolbook.api.application.collection.dto;

import java.util.List;
import java.util.stream.Collectors;

public record CollectionsDTO(Long id, String name, List<String> covers) {

    public static List<CollectionsDTO> from(List<BookCoverDataInCollectionDTO> dtoList) {
        record GroupingKey(Long id, String name) {}

        return dtoList.stream()
            .collect(Collectors.groupingBy(data -> new GroupingKey(data.id(), data.name()),
                Collectors.mapping(BookCoverDataInCollectionDTO::cover, Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new CollectionsDTO(entry.getKey().id(), entry.getKey().name(),
                entry.getValue())).toList();

    }
}
