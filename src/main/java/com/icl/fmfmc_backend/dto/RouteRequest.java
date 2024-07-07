package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.entity.enums.EnumUtils;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
  // TODO: add connectortype, speed, network operator, minNoChargePoints

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Minimum charge level must be non-negative")
  @Max(value = 100, message = "Minimum charge level must be less than or equal to 100")
  @JsonProperty("minChargeLevel")
  private Double minChargeLevel = 20.0;


  @JsonProperty("connectionTypes")
  private List<ConnectionType> connectionTypes;

  @JsonSetter("connectionTypes")
  public void setConnectionTypes(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.connectionTypes = options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(ConnectionType.class,option))
              .collect(Collectors.toList());
    }
  }

  @Min(value = 0, message = "Minimum charge speed must be non-negative")
  @Max(value = 350, message = "Minimum charge speed must be less than or equal to 350")
  @JsonProperty("minKwChargeSpeed")
  private Integer minKwChargeSpeed;

  @Min(value = 0, message = "Maximum charge speed must be greater than 0")
  @Max(value = 350, message = "Maximum charge speed must be less than or equal to 350")
  @JsonProperty("maxKwChargeSpeed")
  private Integer maxKwChargeSpeed;

  @Min(value = 1, message = "Minimum number of charging points must be greater than 0")
  @JsonProperty("minNoChargePoints")
  private Integer minNoChargePoints;

  // Operator/Network



  // dining preferences
  // TODO: add cuisine type, price range, etc.

  @JsonProperty("eatingOptions")
  private List<FoodCategory> eatingOptions = getDefaultEatingOptions();

  @JsonSetter("eatingOptions")
  public void setEatingOptions(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.eatingOptions = options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(FoodCategory.class,option))
              .collect(Collectors.toList());
    }
  }

  private List<FoodCategory> getDefaultEatingOptions() {
    return List.of(FoodCategory.RESTAURANT, FoodCategory.CAFE, FoodCategory.BAR, FoodCategory.FOOD_RETAILER);
  }


  @Min(value = 1, message = "Minimum Price range must be non-negative")
  @Max(value = 4, message = "Minimum Price range must be less than or equal to 4")
  @JsonProperty("minPrice")
  private Integer minPrice;

  @Min(value = 1, message = "Maximum Price range must be non-negative")
  @Max(value = 4, message = "Maximum Price range must be less than or equal to 4")
  @JsonProperty("maxPrice")
  private Integer maxPrice;

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 200, message = "Max walking distance must be equal or greater than 200")
  @Max(value = 500, message = "Max walking distance must be less than or equal to 500")
  @JsonProperty("maxWalkingDistance")
  private Integer maxWalkingDistance = 500;



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
