package com.icl.fmfmc_backend.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.icl.fmfmc_backend.entity.enums.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

// @Validated

//@NoArgsConstructor
@Data
public class ChargerRequest {

  // BoundingBox


  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Top Left Latitude is required")
  @JsonProperty("top_left_lat")
  private Double topLeftLat;

  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Top Left Longitude is required")
  @JsonProperty("top_left_long")
  private Double topLeftLong;

  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Bottom Right Latitude is required")
  @JsonProperty("bottom_right_lat")
  private Double bottomRightLat;

  @Min(value = -90, message = "Latitude must be greater than or equal to -90")
  @Max(value = 90, message = "Latitude must be less than or equal to 90")
  @NotNull(message = "Bottom Right Longitude is required")
  @JsonProperty("bottom_right_long")
  private Double bottomRightLong;

  // vehicle info

  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("electric_vehicle_id")
  private Long electricVehicleId = null;

  // charging preferences

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

  @Min(value = 0, message = "Minimum charge speed must be non-negative")
  @Max(value = 350, message = "Minimum charge speed must be less than or equal to 350")
  @JsonProperty("min_kw_charge_speed")
  private Integer minKwChargeSpeed;

  @Min(value = 1, message = "Maximum charge speed must be greater than 0")
  @Max(value = 350, message = "Maximum charge speed must be less than or equal to 350")
  @JsonProperty("max_kw_charge_speed")
  private Integer maxKwChargeSpeed;

  @Min(value = 1, message = "Minimum number of charging points must be greater than 0")
  @JsonProperty("min_no_charge_points")
  private Integer minNoChargePoints;

}
