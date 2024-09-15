package com.icl.fmfmc_backend.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.icl.fmfmc_backend.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

// @Validated

// @NoArgsConstructor
@Data
public class JourneyRequest {

  // location

  @Schema(description = "Starting destination latitude", example = "51.5074")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Start latitude is required")
  @JsonProperty("start_lat")
  private Double startLat;

  @Schema(description = "Starting destination longitude", example = "-0.1278")
  @Min(value = -180, message = "Latitude must be greater than or equal to -180")
  @Max(value = 180, message = "Longitude must be less than or equal to 180")
  @NotNull(message = "Start longitude is required")
  @JsonProperty("start_long")
  private Double startLong;

  @Schema(description = "Ending destination latitude", example = "48.8566")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "End latitude is required")
  @JsonProperty("end_lat")
  private Double endLat;

  @Schema(description = "Ending destination longitude", example = "2.3522")
  @Min(value = -180, message = "Latitude must be greater than or equal to -180")
  @Max(value = 180, message = "Longitude must be less than or equal to 180")
  @NotNull(message = "End longitude is required")
  @JsonProperty("end_long")
  private Double endLong;

  // vehicle info

  @Schema(description = "Electric vehicle ID")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("electric_vehicle_id")
  private Long electricVehicleId = null;

  @Schema(description = "Starting battery level", example = "1.0")
  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Starting battery must be greater than 0")
  @Max(value = 1, message = "Starting battery must be less than or equal to 1")
  @JsonProperty("starting_battery")
  private Double startingBattery = 1.0;

  @Schema(description = "Electric vehicle range in meters", example = "300000.0")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "EV range is required")
  @Min(value = 1, message = "EV range must be greater than 0")
  @JsonProperty("ev_range") // metres
  private Double evRange = 300000.0;

  @Schema(
      description =
          "Battery capacity in kWh, if not provided it will be estimated based on ev_range",
      example = "75.0")
  @Min(value = 1, message = "Battery capacity must be greater than 0")
  @JsonProperty("battery_capacity") // kWh
  private Double batteryCapacity;

  // charging preferences
  // TODO: network operator

  @Schema(
      description = "Minimum charge level before stopping to recharge",
      example = "0.2",
      defaultValue = "0.2")
  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Minimum charge level must be non-negative")
  @Max(value = 1, message = "Minimum charge level must be less than or equal to 1")
  @JsonProperty("min_charge_level")
  private Double minChargeLevel = 0.2;

  @Schema(
      description = "Maximum charge level to recharge to",
      example = "0.8",
      defaultValue = "0.8")
  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 0, message = "Charge level after each stop must be non-negative")
  @Max(value = 1, message = "Charge level after each stop must be less than or equal to 1")
  @JsonProperty("charge_level_after_each_stop")
  private Double chargeLevelAfterEachStop = 0.8;

  @Schema(
      description = "Targeted charge level on arrival at final destination",
      example = "0.6",
      defaultValue = "0.2")
  @JsonSetter(nulls = Nulls.SKIP)
  @DecimalMin(
      value = "0",
      message = "Final destination charge level after each stop must be non-negative")
  @DecimalMax(
      value = "0.9",
      message = "Final destination charge level after each stop must be less or equal to 0.9")
  @JsonProperty("final_destination_charge_level")
  private Double finalDestinationChargeLevel = 0.2;

  @JsonIgnore
  @AssertTrue(
      message =
          "Final destination charge level must not be higher than charge level after each stop")
  public boolean isFinalDestinationChargeLevelValid() {
    return finalDestinationChargeLevel <= chargeLevelAfterEachStop;
  }

  @Schema(
      description = "Connection types, if not provided all are selected by default",
      example = "css")
  @JsonProperty("connection_types")
  private List<ConnectionType> connectionTypes;

  @JsonSetter("connection_types")
  public void setConnectionTypes(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.connectionTypes =
          options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(ConnectionType.class, option))
              .collect(Collectors.toList());
    }
  }

  @Schema(
      description = "Access types, if not provided all are selected by default",
      example = "public")
  @JsonProperty("access_types")
  private List<AccessType> accessTypes;

  @JsonSetter("access_types")
  public void setAccessTypes(List<String> options) {
    if (options != null && !options.isEmpty()) {
      this.accessTypes =
          options.stream()
              .map(option -> EnumUtils.getEnumFromApiName(AccessType.class, option))
              .collect(Collectors.toList());
    }
  }

  @Schema(description = "Minimum charge speed in kW", example = "50")
  @Min(value = 0, message = "Minimum charge speed must be non-negative")
  @Max(value = 350, message = "Minimum charge speed must be less than or equal to 350")
  @JsonProperty("min_kw_charge_speed")
  private Integer minKwChargeSpeed;

  @Schema(description = "Maximum charge speed in kW", example = "150")
  @Min(value = 1, message = "Maximum charge speed must be greater than 0")
  @Max(value = 350, message = "Maximum charge speed must be less than or equal to 350")
  @JsonProperty("max_kw_charge_speed")
  private Integer maxKwChargeSpeed;

  @Schema(
      description =
          "Number of individual charging bays for an EV, distinct from number of charging connectors",
      example = "2")
  @Min(value = 1, message = "Minimum number of charging points must be greater than 0")
  @JsonProperty("min_no_charge_points")
  private Integer minNoChargePoints;

  // Operator/Network

  // dining preferences

  @Schema(
      description = "Determines if FMFMC will search for a food establishment",
      example = "true",
      defaultValue = "true")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("stop_for_eating")
  private Boolean stopForEating = true;

  @Schema(
      description = "Types of food establishments to search for",
      example = "pizza",
      defaultValue = "restaurant, cafe, bar, food_retailer")
  @JsonProperty("eating_options")
  private List<FoodCategory> eatingOptions = getDefaultEatingOptions();

  @JsonSetter("eating_options")
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

  @Schema(description = "Minimum price range for eating options", example = "1", defaultValue = "1")
  @Min(value = 1, message = "Minimum Price range must be non-negative")
  @Max(value = 4, message = "Minimum Price range must be less than or equal to 4")
  @JsonProperty("min_price")
  private Integer minPrice;

  @Schema(description = "Maximum price range for eating options", example = "4", defaultValue = "4")
  @Min(value = 1, message = "Maximum Price range must be non-negative")
  @Max(value = 4, message = "Maximum Price range must be less than or equal to 4")
  @JsonProperty("max_price")
  private Integer maxPrice;

  @Schema(
      description = "Max walking distance between suggested eating option and charger in metres",
      example = "2000",
      defaultValue = "500")
  @JsonSetter(nulls = Nulls.SKIP)
  @Min(value = 200, message = "Max walking distance must be equal or greater than 200")
  @Max(value = 5000, message = "Max walking distance must be less than or equal to 500")
  @JsonProperty("max_walking_distance")
  private Integer maxWalkingDistance = 500;

  @Schema(
      description =
          "If true, an additional four nearby eating options within max_walking_distance of the charger adjacent to the suggested eating option are returned",
      example = "true",
      defaultValue = "false")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("include_alternative_eating_options")
  private Boolean includeAlternativeEatingOptions = false;

  // time constraints

  @Schema(description = "Departure time", example = "09:00", defaultValue = "current time")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("depart_time")
  private LocalTime departTime = LocalTime.now();

  //  @JsonSetter(nulls = Nulls.SKIP)
  //  @JsonProperty("meal_time")
  //  private LocalTime mealTime = null;

  @Schema(
      description =
          "Time spent at suggested eating option used in overall journey duration calculation",
      example = "01:30",
      defaultValue = "01:00")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("break_duration")
  private LocalTime breakDuration = LocalTime.of(1, 0);

  @Schema(
      description = "Target portion of journey to stop for eating option",
      example = "latest",
      defaultValue = "middle")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("stopping_range")
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

  @Schema(
      description =
          "Determines how far from the optimal route the application will search for chargers ",
      example = "minimal",
      defaultValue = "moderate")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("charger_search_deviation")
  private DeviationScope chargerSearchDeviation = DeviationScope.moderate;

  @Schema(
      description =
          "Determines how far from the optimal route the application will search for eating options ",
      example = "minimal",
      defaultValue = "moderate")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("eating_option_search_deviation")
  private DeviationScope eatingOptionSearchDeviation = DeviationScope.moderate;

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
