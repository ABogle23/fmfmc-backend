package com.icl.fmfmc_backend.dto.Api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.icl.fmfmc_backend.dto.ElectricVehicle;
import com.icl.fmfmc_backend.entity.enums.*;
import com.icl.fmfmc_backend.service.ElectricVehicleService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

// @Validated
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
  @JsonProperty("electricVehicleId")
  private Long electricVehicleId = null;

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Starting battery must be greater than 0")
  @Max(value = 1, message = "Starting battery must be less than or equal to 1")
  @JsonProperty("startingBattery")
  private Double startingBattery = 1.0;

  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "EV range is required")
  @Min(value = 1, message = "EV range must be greater than 0")
  @JsonProperty("evRange") // metres
  private Double evRange = 300000.0;

  @Min(value = 1, message = "Battery capacity must be greater than 0")
  @JsonProperty("batteryCapacity") // kWh
  private Double batteryCapacity;

  // charging preferences
  // TODO: network operator

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Minimum charge level must be non-negative")
  @Max(value = 1, message = "Minimum charge level must be less than or equal to 1")
  @JsonProperty("minChargeLevel")
  private Double minChargeLevel = 0.2;

  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Charge level after each stop must be non-negative")
  @Max(value = 1, message = "Charge level after each stop must be less than or equal to 1")
  @JsonProperty("chargeLevelAfterEachStop")
  private Double chargeLevelAfterEachStop = 0.8;

  @JsonSetter(nulls = Nulls.SKIP)
  @DecimalMin(
      value = "0",
      message = "Final destination charge level after each stop must be non-negative")
  @DecimalMax(
      value = "0.9",
      message = "Final destination charge level after each stop must be less or equal to 0.9")
  @JsonProperty("finalDestinationChargeLevel")
  private Double finalDestinationChargeLevel = 0.2;

  @AssertTrue(
      message =
          "Final destination charge level must not be higher than charge level after each stop")
  public boolean isFinalDestinationChargeLevelValid() {
    return finalDestinationChargeLevel <= chargeLevelAfterEachStop;
  }

  @JsonProperty("connectionTypes")
  private List<ConnectionType> connectionTypes;

  @JsonSetter("connectionTypes")
  public void setConnectionTypes(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.connectionTypes =
          options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(ConnectionType.class, option))
              .collect(Collectors.toList());
    }
  }

  @JsonProperty("accessTypes")
  private List<AccessType> accessTypes;

  @JsonSetter("accessTypes")
  public void setAccessTypes(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.accessTypes =
          options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(AccessType.class, option))
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
      this.eatingOptions =
          options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(FoodCategory.class, option))
              .collect(Collectors.toList());
    }
  }

  private List<FoodCategory> getDefaultEatingOptions() {
    return List.of(
        FoodCategory.RESTAURANT, FoodCategory.CAFE, FoodCategory.BAR, FoodCategory.FOOD_RETAILER);
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
  @Max(value = 5000, message = "Max walking distance must be less than or equal to 500")
  @JsonProperty("maxWalkingDistance")
  private Integer maxWalkingDistance = 500;

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("includeAlternativeEatingOptions")
  private Boolean includeAlternativeEatingOptions = false;

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

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("stoppingRange")
  private StoppingRange stoppingRange = StoppingRange.middle;

  public Double[] getStoppingRangeAsFraction() {
    if (this.stoppingRange == null) {
      return new Double[] {0.33, 0.67}; // default to middle if null
    }
    return switch (this.stoppingRange) {
      case earliest -> new Double[] {0.05, 0.33};
      case early -> new Double[] {0.25, 0.5};
      case middle -> new Double[] {0.33, 0.67};
      case later -> new Double[] {0.5, 0.75};
      case latest -> new Double[] {0.67, 0.95};
      default -> new Double[] {0.33, 0.67}; // default to middle
    };
  }

  // Route search preference

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("chargerSearchDeviation")
  private DeviationScope chargerSearchDeviation = DeviationScope.moderate;

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("eatingOptionSearchDeviation")
  private DeviationScope eatingOptionSearchDeviation = DeviationScope.moderate;

  // mot implemented due to potential routing service algo issues
  public Double getChargerSearchDeviationAsFraction() {
    if (this.chargerSearchDeviation == null) {
      return 6.5; // 6.5km
    }
    return switch (this.chargerSearchDeviation) {
      case minimal -> 3.5;
      case moderate -> 6.5;
      case significant -> 10.5;
      case extreme -> 20.5;
      default -> 6.5; // default to moderate
    };
  }

  public Double getEatingOptionSearchDeviationAsFraction() {
    if (this.eatingOptionSearchDeviation == null) {
      return 6.5; // 1.5km
    }
    return switch (this.eatingOptionSearchDeviation) {
      case minimal -> 3.5;
      case moderate -> 6.5;
      case significant -> 10.5;
      case extreme -> 20.5;
      default -> 6.5; // default to moderate
    };
  }
}
