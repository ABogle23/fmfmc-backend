package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

//https://docs.foursquare.com/developer/reference/place-search

@Data
public class FoursquareResponseDTO {

  @JsonProperty("results")
  public List<FoursquarePlaceDTO> results;

  @Data
  public static class FoursquarePlaceDTO {
    @JsonProperty("fsq_id")
    private String fsqId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("categories")
    private List<CategoryDTO> categories;

    @JsonProperty("geocodes")
    private GeocodeDTO geocodes;

    @JsonProperty("location")
    private LocationDTO location;

    @JsonProperty("closed_bucket")
    private String closedBucket;

    @JsonProperty("popularity")
    private Double popularity;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("rating")
    private Double rating;

    @Data
    public static class CategoryDTO {

      @JsonProperty("id")
      private Long id;

      @JsonProperty("name")
      private String name;
    }

    @Data
    public static class GeocodeDTO {

      @JsonProperty("main")
      private MainDTO main;

      @Data
      public static class MainDTO {

        @JsonProperty("latitude")
        public Double latitude;

        @JsonProperty("longitude")
        public Double longitude;
      }
    }

    @Data
    public static class LocationDTO {
      @JsonProperty("address")
      public String address;

      @JsonProperty("country")
      public String country;

      @JsonProperty("formatted_address")
      public String formattedAddress;

      @JsonProperty("locality")
      public String locality;

      @JsonProperty("postcode")
      public String postcode;

      @JsonProperty("region")
      public String region;
    }
  }
}
