package com.gomdolbook.api.config;

import com.gomdolbook.api.api.dto.APIError;
import com.gomdolbook.api.errors.BookNotFoundException;
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

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, errors);

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error = ex.getName() + " should be of type" + ex.getRequiredType().getName();

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, error);

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex,
        WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() +
                violation.getPropertyPath() + ": " + violation.getMessage());
        }

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, WebRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ex.getMethod());
        stringBuilder.append(
            " method is not supported for this request. Supported methods are "
        );
        Optional.of(ex.getSupportedHttpMethods())
            .ifPresentOrElse(httpMethods -> httpMethods.forEach(
                    httpMethod -> stringBuilder.append(httpMethod).append(" ")),
                () -> stringBuilder.append("No Supported HTTP method available"));

        APIError apiError = new APIError(HttpStatus.METHOD_NOT_ALLOWED, stringBuilder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
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

        APIError apiError = new APIError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            stringBuilder.toString());

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

    }

    @ExceptionHandler({BookNotFoundException.class})
    public ResponseEntity<Object> handleBookNotFound(BookNotFoundException ex, WebRequest request) {
        String error = ex.getMessage();
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({AuthorizationDeniedException.class})
    public ResponseEntity<Object> handleAuthorizationDeniedException(Exception ex,
        WebRequest request) {
        APIError apiError = new APIError(HttpStatus.FORBIDDEN, "Access Denied");
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        APIError apiError = new APIError(HttpStatus.INTERNAL_SERVER_ERROR, "error occurred");
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
