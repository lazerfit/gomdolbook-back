package com.gomdolbook.api.domain.shared;

public class BookNotInCollectionException extends CustomErrorMessage{

    public BookNotInCollectionException(String causedParameter) {
        super(causedParameter);
    }
}
