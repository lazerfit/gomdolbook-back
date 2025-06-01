package com.gomdolbook.api.domain.models.readinglog;

public interface ReadingLogRepositoryCustom {
    Integer getRating(String isbn, String email);
}
