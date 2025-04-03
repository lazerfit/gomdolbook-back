package com.gomdolbook.api.domain.shared;

public class UserValidationError extends CustomErrorMessage{

    public UserValidationError(String causedParameter) {
        super(causedParameter);
    }

    public UserValidationError(String message, Throwable cause) {
        super(message, cause);
    }
}
