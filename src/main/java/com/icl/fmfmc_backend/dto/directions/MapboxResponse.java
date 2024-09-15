package com.icl.fmfmc_backend.dto.directions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.icl.fmfmc_backend.dto.LineStringDeserializer;
import lombok.Data;
import org.locationtech.jts.geom.LineString;

@Data
public class MapboxResponse {

  @JsonProperty("routes")
  private List<RouteDTO> routes;

  @JsonProperty("waypoints")
  private List<WaypointDTO> waypoints;

  @JsonProperty("code")
  private String code;

  @JsonProperty("uuid")
  private String uuid;

  @Data
  public static class RouteDTO {

    @JsonProperty("weight_name")
    private String weightName;

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("duration")
    private Double duration;

    @JsonProperty("distance")
    private Double distance;

    @JsonProperty("legs")
    private List<LegDTO> legs;

    @JsonProperty("geometry")
    private GeometryDTO geometry;
  }

  @Data
  public static class LegDTO {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("duration")
    private Double duration;

    @JsonProperty("distance")
    private Double distance;
  }

  @Data
  public static class GeometryDTO {

    @JsonDeserialize(using = LineStringDeserializer.class)
    @JsonProperty("coordinates")
    private LineString coordinates;

    private String type;
  }

  @Data
  public static class WaypointDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private List<Double> location;

    @JsonProperty("distance")
    private Double distance;
  }
}
