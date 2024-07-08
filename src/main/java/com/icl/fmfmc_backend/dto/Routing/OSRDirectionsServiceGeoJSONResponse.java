package com.icl.fmfmc_backend.dto.Routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icl.fmfmc_backend.dto.GeoCoordinatesDeserializer;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import lombok.Data;
import org.locationtech.jts.geom.LineString;

@Data
public class OSRDirectionsServiceGeoJSONResponse {

  @JsonProperty("type")
  private String type;

  @JsonProperty("bbox")
  private List<Double> bbox;

  @JsonProperty("features")
  private List<FeatureDTO> features;

  @Data
  public static class FeatureDTO {

    @JsonProperty("bbox")
    private List<Double> bbox;

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private PropertiesDTO properties;

    @JsonProperty("geometry")
    private GeometryDTO geometry;

    @Data
    public static class PropertiesDTO {

      @JsonProperty("segments")
      private List<SegmentDTO> segments;

      @JsonProperty("way_points")
      private List<Integer> wayPoints;

      @JsonProperty("summary")
      private SummaryDTO summary;

      @Data
      public static class SegmentDTO {

        @JsonProperty("distance")
        private Double distance;

        @JsonProperty("duration")
        private Double duration;

        @JsonProperty("steps")
        private List<StepDTO> steps;

        @JsonProperty("detourfactor")
        private Double detourFactor;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("avgspeed")
        private Double avgSpeed;

        @Data
        public static class StepDTO {

          @JsonProperty("distance")
          private Double distance;

          @JsonProperty("duration")
          private Double duration;

          @JsonProperty("type")
          private Integer type;

          @JsonProperty("instruction")
          private String instruction;

          @JsonProperty("name")
          private String name;

          @JsonProperty("way_points")
          private List<Integer> wayPoints;
        }
      }

      @Data
      public static class SummaryDTO {

        @JsonProperty("distance")
        private Double distance;

        @JsonProperty("duration")
        private Double duration;
      }
    }

    @Data
    public static class GeometryDTO {

      @JsonProperty("coordinates")
      @JsonDeserialize(using = GeoCoordinatesDeserializer.class)
      private List<GeoCoordinates> coordinates;

      @JsonProperty("type")
      private String type;
    }
  }
}
