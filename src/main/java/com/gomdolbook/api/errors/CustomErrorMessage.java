package com.gomdolbook.api.errors;

public class CustomErrorMessage extends RuntimeException{

    protected CustomErrorMessage(String causedParameter) {
        super("Can't find Book: " + causedParameter);
    }
}
