package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalTime;

//@Validated
@Data
public class RouteRequest {

  // location

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

    // vehicle info

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Starting battery must be non-negative")
  @Max(value = 100, message = "Starting battery must be non-negative")
  @JsonProperty("startingBattery")
  private Double startingBattery = 100.0;

  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "EV range is required")
  @Min(value = 1, message = "EV range must be greater than 0")
  @JsonProperty("evRange")
  private Double evRange = 300.0;

  // charging preferences
  // TODO: add connectortype

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Minimum charge level must be non-negative")
  @Max(value = 100, message = "Minimum charge level must be less than or equal to 100")
  @JsonProperty("minChargeLevel")
  private Double minChargeLevel = 20.0;

  // dining preferences
  // TODO: add cuisine type, price range, etc.

  // time constraints

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("departTime")
  private LocalTime departTime = LocalTime.now();

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("mealTime")
  private LocalTime mealTime = null;

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("breakDuration")
  private LocalTime breakDuration = LocalTime.of(1, 0);


}
