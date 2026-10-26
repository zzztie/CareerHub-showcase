package com.zzztie.careerhub.exception;

import com.zzztie.careerhub.dto.ApiResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        ApiResponse<List<String>> response = new ApiResponse<>(
                400,
                "validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleMethodValidationException(
            HandlerMethodValidationException e) {
        List<String> errors = e.getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        ApiResponse<List<String>> response = new ApiResponse<>(
                400,
                "validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
            UserNotFoundException e) {

        ApiResponse<Object> response = new ApiResponse<>(
                404,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        ApiResponse<Object> response = new ApiResponse<>(
                400,
                "invalid parameter type",
                null
        );
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentialsException(InvalidCredentialsException e){
        ApiResponse<Object> response = new ApiResponse<>(401,e.getMessage(),null);
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidTokenException(
            InvalidTokenException e
    ){
        ApiResponse<Object> response = new ApiResponse<>(
                401,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(401).body(response);
    }
    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResumeNotFoundException(
            ResumeNotFoundException e
    ){
        ApiResponse<Object> response = new ApiResponse<>(
                404,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(404).body(response);
    }
    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleJobNotFoundException(
            JobNotFoundException e){
        ApiResponse<Object> response = new ApiResponse<>(
                404,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(404).body(response);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException e) {

        ApiResponse<Object> response = new ApiResponse<>(
                400,
                e.getMessage(),
                null
        );

        return ResponseEntity.badRequest().body(response);
    }
}