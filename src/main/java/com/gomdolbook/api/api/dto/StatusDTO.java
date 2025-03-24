package com.gomdolbook.api.api.dto;

public record StatusDTO(String status) {

    public static StatusDTO of(String status) {
        return new StatusDTO(status);
    }
}
