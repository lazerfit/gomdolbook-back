package com.gomdolbook.api.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CollectionResponseDto {

    private final String name;
    private final List<CollectionBookResponseDTO> books = new ArrayList<>();

    public CollectionResponseDto(String name) {
        this.name = name;
    }
}
