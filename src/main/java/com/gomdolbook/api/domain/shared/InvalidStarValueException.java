package com.gomdolbook.api.domain.shared;

public class InvalidStarValueException extends CustomErrorMessage{

    public InvalidStarValueException(String causedParameter) {
        super(causedParameter);
    }
}
