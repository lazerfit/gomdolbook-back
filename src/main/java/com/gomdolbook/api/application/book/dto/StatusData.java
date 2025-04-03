package com.gomdolbook.api.application.book.dto;

public record StatusData(String status) {

    public static StatusData of(String status) {
        return new StatusData(status);
    }
}
