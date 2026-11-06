package com.project.app.exceptions;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.project.app.payloads.ApiResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.LockedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResouceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundExceptionHandler(ResouceNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgsNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            response.put(fieldName, message);
        });
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> apiExceptionHandler(ApiException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false), ex.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> badCredentialsExceptionHandler(BadCredentialsException ex) {
        return new ResponseEntity<>(new ApiResponse("invalid email or password", false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse> lockedExceptionHandler(LockedException ex) {
        return new ResponseEntity<>(new ApiResponse("account is locked because of too many failed logins, try again later", false), HttpStatus.LOCKED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> accessDeniedExceptionHandler(AccessDeniedException ex) {
        return new ResponseEntity<>(new ApiResponse("you are not allowed to perform this action", false), HttpStatus.FORBIDDEN);
    }

    // e.g. deleting a category that still has posts
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> dataIntegrityExceptionHandler(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(new ApiResponse("request conflicts with existing data", false), HttpStatus.CONFLICT);
    }

    // e.g. sortBy=someFieldThatDoesNotExist
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse> invalidPropertyExceptionHandler(PropertyReferenceException ex) {
        return new ResponseEntity<>(new ApiResponse("invalid sort field : " + ex.getPropertyName(), false), HttpStatus.BAD_REQUEST);
    }

    // requested image does not exist on disk
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse> fileNotFoundExceptionHandler(FileNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse("file not found", false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> maxUploadSizeExceptionHandler(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(new ApiResponse("uploaded file is too large", false), HttpStatus.CONTENT_TOO_LARGE);
    }

    // somebody else saved the same record between our read and write
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse> optimisticLockExceptionHandler(ObjectOptimisticLockingFailureException ex) {
        return new ResponseEntity<>(new ApiResponse("this record was changed by someone else, reload and try again", false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> unreadableBodyExceptionHandler(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(new ApiResponse("request body is missing or malformed", false), HttpStatus.BAD_REQUEST);
    }
}
