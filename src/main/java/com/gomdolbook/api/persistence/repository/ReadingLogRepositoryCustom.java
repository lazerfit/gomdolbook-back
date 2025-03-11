package com.gomdolbook.api.persistence.repository;

public interface ReadingLogRepositoryCustom {
    Integer getRating(String isbn, String email);
}
