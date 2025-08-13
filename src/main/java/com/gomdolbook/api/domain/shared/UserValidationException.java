package com.gomdolbook.api.domain.shared;

public class UserValidationException extends CustomErrorMessage{

    public UserValidationException(String causedParameter) {
        super(causedParameter);
    }

    public UserValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
