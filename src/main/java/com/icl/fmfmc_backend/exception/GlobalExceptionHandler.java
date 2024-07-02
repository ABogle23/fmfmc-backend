package com.icl.fmfmc_backend.exception;

import com.icl.fmfmc_backend.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // handles all validation errors where @Valid annotations are present.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(this::formatErrorMessage)
                .collect(Collectors.toList());
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed", details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String formatErrorMessage(ObjectError error) {
        if (error instanceof FieldError) {
            return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
        } else {
            return error.getDefaultMessage();
        }
    }

    // TODO: more exceptions
}