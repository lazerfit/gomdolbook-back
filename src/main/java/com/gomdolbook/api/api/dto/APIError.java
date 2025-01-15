package com.gomdolbook.api.api.dto;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class APIError {

    private final HttpStatus status;
    private final List<String> errors;

    public APIError(HttpStatus status, List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public APIError(HttpStatus status, String errors) {
        this.status = status;
        this.errors = Collections.singletonList(errors);
    }
}
