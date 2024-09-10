package com.icl.fmfmc_backend.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.icl.fmfmc_backend.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

// @Validated

// @NoArgsConstructor
@Data
@Schema(description = "Charger request object containing charger search parameters")
public class ChargerRequest {

  // BoundingBox

  @Schema(description = "Top Left Latitude", example = "51.5074")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Top Left Latitude is required")
  @JsonProperty("top_left_lat")
  private Double topLeftLat;

  @Schema(description = "Top Left Longitude", example = "-0.1278")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Top Left Longitude is required")
  @JsonProperty("top_left_long")
  private Double topLeftLong;

  @Schema(description = "Bottom Right Latitude", example = "51.5074")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Bottom Right Latitude is required")
  @JsonProperty("bottom_right_lat")
  private Double bottomRightLat;

  @Schema(description = "Bottom Right Longitude", example = "-0.1278")
  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Bottom Right Longitude is required")
  @JsonProperty("bottom_right_long")
  private Double bottomRightLong;

  // vehicle info

//  @JsonSetter(nulls = Nulls.SKIP)
//  @JsonProperty("electric_vehicle_id")
//  private Long electricVehicleId = null;

  // charging preferences

  @Schema(description = "Connection types, if not provided all are selected by default", example = "css")
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

  @Schema(description = "Access types, if not provided all are selected by default", example = "public")

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

  @Schema(description = "If true, a subset of charger object fields {id, location} are returned", example = "true", defaultValue = "true")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("compact")
  Boolean compact = true;
}
