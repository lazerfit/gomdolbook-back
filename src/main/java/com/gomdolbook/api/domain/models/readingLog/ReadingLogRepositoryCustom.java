package com.gomdolbook.api.domain.models.readingLog;

public interface ReadingLogRepositoryCustom {
    Integer getRating(String isbn, String email);
}
