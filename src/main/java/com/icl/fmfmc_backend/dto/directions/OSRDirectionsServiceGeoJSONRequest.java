package com.icl.fmfmc_backend.dto.directions;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class OSRDirectionsServiceGeoJSONRequest {

  // default constructor
  private OSRDirectionsServiceGeoJSONRequest() {
    //    this.alternativeRoutes = new AlternativeRoutes(1);
    this.attributes = List.of("avgspeed", "detourfactor", "percentage");
    this.geometrySimplify = "true";
    this.preference = "recommended";
    this.units = "m";
  }

  // additional constructor that allows overriding default values
  public OSRDirectionsServiceGeoJSONRequest(List<Double[]> coordinates) {
    this(); // call the default constructor to set defaults
    this.coordinates = coordinates; // override coordinates
  }

  //  @NotNull(message = "Coordinates are required")
  @JsonProperty("coordinates")
  private List<Double[]> coordinates;

  //  @JsonProperty("alternative_routes")
  //  private AlternativeRoutes alternativeRoutes;

  @JsonProperty("attributes")
  private List<String> attributes;

  @JsonProperty("geometry_simplify")
  private String geometrySimplify;

  @JsonProperty("preference")
  private String preference;

  @JsonProperty("units")
  private String units;

//  @AllArgsConstructor
//  @Data
//  public static class AlternativeRoutes {
//    @JsonProperty("target_count")
//    private Integer targetCount;
//  }
}
