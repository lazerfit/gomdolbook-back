package com.gomdolbook.api.errors;

public class BookNotFoundException extends CustomErrorMessage{

    public BookNotFoundException(String message) {
        super("Can't find Book: " + message);
    }
}
