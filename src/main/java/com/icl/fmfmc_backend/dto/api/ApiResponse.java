package com.icl.fmfmc_backend.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
// @AllArgsConstructor
@Schema(description = "API Response wrapper")
public class ApiResponse<T> {
  @Schema(description = "Details of journey", implementation = RouteResult.class)
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
