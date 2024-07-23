package com.icl.fmfmc_backend.dto.Api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
// @AllArgsConstructor
public class ApiResponse<T> {
  private T data;
  private boolean success;
  private String message;
  private LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Boolean fallbackUsed;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String fallbackDetails;

  public ApiResponse(T data, Boolean success, String message) {
    this.data = data;
    this.success = success;
    this.message = message;
    this.fallbackUsed = null;
    this.fallbackDetails = null;
  }

  public ApiResponse(
      T data, Boolean success, String message, Boolean fallbackUsed, String fallbackDetails) {
    this.data = data;
    this.success = success;
    this.message = message;
    this.fallbackUsed = fallbackUsed;
    this.fallbackDetails = fallbackDetails;
  }
}
