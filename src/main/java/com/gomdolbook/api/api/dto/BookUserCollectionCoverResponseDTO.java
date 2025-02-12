package com.gomdolbook.api.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class BookUserCollectionCoverResponseDTO {

    private final String collectionName;
    private final List<String> bookCovers = new ArrayList<>();

    public BookUserCollectionCoverResponseDTO(String collectionName) {
        this.collectionName = collectionName;
    }

    public void addBookCover(String cover) {
        if (!bookCovers.contains(cover)) {
            bookCovers.add(cover);
        }
    }
}
