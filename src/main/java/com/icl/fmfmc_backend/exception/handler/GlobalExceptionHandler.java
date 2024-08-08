package com.icl.fmfmc_backend.exception.handler;

import com.icl.fmfmc_backend.dto.api.ApiErrorResponse;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
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
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getAllErrors().stream()
            .map(this::formatErrorMessage)
            .collect(Collectors.toList());
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed", details);
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
  public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    String error = "Invalid input provided.";
    String field = inferFieldFromException(ex); // infer the field from exception msg

    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Validation Failed", List.of(field + ": " + error));
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  private String inferFieldFromException(HttpMessageNotReadableException ex) {
    if (ex.getCause().getMessage().contains("eating_options")) {
      return "eating_options";
    } else if (ex.getCause().getMessage().contains("connector_type")) {
      return "connector_type";
    } else if (ex.getCause().getMessage().contains("stopping_range")) {
      return "stopping_range";
    } else if (ex.getCause().getMessage().contains("charger_search_deviation")) {
      return "charger_search_deviation";
    } else if (ex.getCause().getMessage().contains("eating_option_search_deviation")) {
      return "eating_option_search_deviation";
    } else if (ex.getCause().getMessage().contains("electric_vehicle_id")) {
      return "electric_vehicle_id";
    } else if (ex.getCause().getMessage().contains("stop_for_eating")) {
      return "stop_for_eating";
    } else if (ex.getCause().getMessage().contains("access_types")) {
      return "access_types";
    }
    return "Unknown";
  }

  @ExceptionHandler(JourneyNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleJourneyNotFoundException(
          JourneyNotFoundException ex) {
    String error = ex.getMessage();

    ApiErrorResponse errorResponse =
            new ApiErrorResponse(
                    HttpStatus.NOT_FOUND.value(), "Please check your input parameters and try again.", List.of(error));
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

}
