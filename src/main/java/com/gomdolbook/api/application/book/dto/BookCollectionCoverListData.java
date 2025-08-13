package com.gomdolbook.api.application.book.dto;

import java.util.List;
import java.util.stream.Collectors;

public record BookCollectionCoverListData
    (Long id, String name, List<String> covers) {

    public static List<BookCollectionCoverListData> from(List<BookCollectionCoverData> dtoList) {
        record GroupingKey(Long id, String name) {}

        return dtoList.stream()
            .collect(Collectors.groupingBy(data -> new GroupingKey(data.id(),
                    data.name()),
                Collectors.filtering(data -> data.cover() != null,
                    Collectors.mapping(BookCollectionCoverData::cover, Collectors.toList()))))
            .entrySet()
            .stream()
            .map(entry -> new BookCollectionCoverListData(entry.getKey()
                .id(), entry.getKey().name(), entry.getValue())).toList();
    }
}
