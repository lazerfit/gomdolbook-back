package com.gomdolbook.api.errors;

public class CustomErrorMessage extends RuntimeException{

    protected CustomErrorMessage(String causedParameter) {
        super(causedParameter);
    }

    protected CustomErrorMessage(String message, Throwable cause) {
        super(message, cause);
    }
}
