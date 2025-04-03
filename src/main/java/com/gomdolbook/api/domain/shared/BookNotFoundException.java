package com.gomdolbook.api.domain.shared;

public class BookNotFoundException extends CustomErrorMessage{

    public BookNotFoundException(String message) {
        super("Can't find Book: " + message);
    }
}
