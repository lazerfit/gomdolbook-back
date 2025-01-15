package com.gomdolbook.api.api.dto;

import java.util.List;

public record AladinAPI(int totalResults,
                        int startIndex,
                        int itemsPerPage,
                        List<Item> item) {

    public record Item(String title,
                       String author,
                       String pubDate,
                       String description,
                       String isbn13,
                       String cover,
                       String categoryName,
                       String publisher) {
        
    }

}
