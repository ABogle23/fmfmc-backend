package com.icl.fmfmc_backend.exception;

import com.icl.fmfmc_backend.dto.Api.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    // handles all validation errors where @Valid annotations are present.
    // https://medium.com/@tericcabrel/validate-request-body-and-parameter-in-spring-boot-53ca77f97fe9
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


    // handles invalid JSON input errors
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String error = "Invalid input provided.";
        String field = inferFieldFromException(ex);  // infer the field from exception msg

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed", List.of(field + ": " + error));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String inferFieldFromException(HttpMessageNotReadableException ex) {
        if (ex.getCause().getMessage().contains("eatingOptions")) {
            return "eatingOptions";
        } else if (ex.getCause().getMessage().contains("connectorType")) {
            return "connectorType";
        } else if (ex.getCause().getMessage().contains("stoppingRange")) {
            return "stoppingRange";
        } else if (ex.getCause().getMessage().contains("chargerSearchDeviation")) {
            return "chargerSearchDeviation";
        } else if (ex.getCause().getMessage().contains("eatingOptionSearchDeviation")) {
            return "eatingOptionSearchDeviation";
        }

        return "Unknown";
    }


}