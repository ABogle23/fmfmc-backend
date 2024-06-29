package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class RouteRequest {
  @NotNull(message = "Start latitude is required")
  @JsonProperty("startLat")
  private Double startLat;

  @NotNull(message = "Start longitude is required")
  @JsonProperty("startLong")
  private Double startLong;

  @NotNull(message = "End latitude is required")
  @JsonProperty("endLat")
  private Double endLat;

  @NotNull(message = "End longitude is required")
  @JsonProperty("endLong")
  private Double endLong;

  @NotNull(message = "Starting Battery is required")
  @Min(value = 0, message = "Starting battery must be non-negative")
  @Max(value = 100, message = "Starting battery must be non-negative")
  @JsonProperty("startingBattery")
  private Integer startingBattery;

  @NotNull(message = "EV range is required")
  @Min(value = 1, message = "EV range must be greater than 0")
  @JsonProperty("evRange")
  private Integer evRange;
}
