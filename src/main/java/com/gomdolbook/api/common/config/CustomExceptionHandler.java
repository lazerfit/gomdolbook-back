package com.gomdolbook.api.common.config;

import com.gomdolbook.api.application.shared.ApiErrorResponse;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.BookNotInCollectionException;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.InvalidStarValueException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, errors);

        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error = ex.getName() + " should be of type" + ex.getRequiredType().getName();

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);

        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex,
        WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() +
                violation.getPropertyPath() + ": " + violation.getMessage());
        }

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, WebRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ex.getMethod());
        stringBuilder.append(
            " method is not supported for this request. Supported methods are "
        );
        Optional.ofNullable(ex.getSupportedHttpMethods())
            .ifPresentOrElse(httpMethods -> httpMethods.forEach(
                    httpMethod -> stringBuilder.append(httpMethod).append(" ")),
                () -> stringBuilder.append("No Supported HTTP method available"));

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, stringBuilder.toString());
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, WebRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ex.getContentType());
        stringBuilder.append(" media type is not supported. supported media types are");
        Optional.of(ex.getSupportedMediaTypes())
            .ifPresentOrElse(types -> types.forEach(type -> stringBuilder.append(type).append(" "))
                , () -> stringBuilder.append("No Supported Media type available"));

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            stringBuilder.toString());

        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());

    }

    @ExceptionHandler({BookNotFoundException.class})
    public ResponseEntity<Object> handleBookNotFound(BookNotFoundException ex, WebRequest request) {
        String error = ex.getMessage();
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({AuthorizationDeniedException.class})
    public ResponseEntity<Object> handleAuthorizationDeniedException(Exception ex,
        WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.FORBIDDEN, "Access Denied");
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error occurred");
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus());
    }

    @ExceptionHandler({CollectionNotFoundException.class})
    public ResponseEntity<Object> handleCollectionNotFound(CollectionNotFoundException ex, WebRequest request) {
        String error = ex.getMessage();
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(),
            apiErrorResponse.getStatus());
    }

    @ExceptionHandler({BookNotInCollectionException.class})
    public ResponseEntity<Object> handleCollectionNotFound(BookNotInCollectionException ex, WebRequest request) {
        String error = ex.getMessage();
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(),
            apiErrorResponse.getStatus());
    }

    @ExceptionHandler({InvalidStarValueException.class})
    public ResponseEntity<Object> handleCollectionNotFound(InvalidStarValueException ex, WebRequest request) {
        String error = ex.getMessage();
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(),
            apiErrorResponse.getStatus());
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<Object> handleCollectionNotFound(ResponseStatusException ex, WebRequest request) {
        String error = ex.getMessage();
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiErrorResponse, new HttpHeaders(),
            apiErrorResponse.getStatus());
    }
}
