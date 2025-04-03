package com.gomdolbook.api.domain.shared;

public class CollectionNotFoundException extends CustomErrorMessage{

    public CollectionNotFoundException(String message) {
        super("Can't find Collection: " + message);
    }
}
