package com.gomdolbook.api.application.book.dto;

import java.util.List;

public record AladinResponseData(int totalResults,
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
