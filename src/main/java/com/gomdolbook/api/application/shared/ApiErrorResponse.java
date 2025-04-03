package com.gomdolbook.api.application.shared;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorResponse {

    private final HttpStatus status;
    private final List<String> errors;

    public ApiErrorResponse(HttpStatus status, List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public ApiErrorResponse(HttpStatus status, String errors) {
        this.status = status;
        this.errors = Collections.singletonList(errors);
    }
}
