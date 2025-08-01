package com.gomdolbook.api.application.shared;

import lombok.Getter;

@Getter
public class ErrorAPIResponse {

    private final String name;
    private final String message;

    public ErrorAPIResponse(String name, String message) {
        this.name = name;
        this.message = message;
    }
}
