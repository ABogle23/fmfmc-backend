package com.icl.fmfmc_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Deprecated
//@AllArgsConstructor
public class Coordinates {
  private Double latitude;
  private Double longitude;

  public Coordinates(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    validate();
  }

  private void validate() {
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Invalid latitude or longitude values");
    }
  }

  @Override
  public String toString() {
    return "Coordinates{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
  }

}
