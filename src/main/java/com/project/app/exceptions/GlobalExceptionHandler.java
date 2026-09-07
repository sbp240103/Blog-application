package com.project.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.app.payloads.ApiResponse;

@RestControllerAdvice  
public class GlobalExceptionHandler {
    
    @ExceptionHandler (ResouceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundExceptionHandler(ResouceNotFoundException ex){
        String message = ex.getMessage();
        ApiResponse apiResponse = new ApiResponse(message, false);
        return new  ResponseEntity<ApiResponse>(apiResponse, HttpStatus.NOT_FOUND);
    }
}
