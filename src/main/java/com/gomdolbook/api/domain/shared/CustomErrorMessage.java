package com.gomdolbook.api.domain.shared;

public class CustomErrorMessage extends RuntimeException{

    protected CustomErrorMessage(String message) {
        super(message);
    }

    protected CustomErrorMessage(String message, Throwable cause) {
        super(message, cause);
    }
}
