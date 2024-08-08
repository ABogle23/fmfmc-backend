package com.icl.fmfmc_backend.dto.charger;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//https://openchargemap.org/site/develop/api#/
@Data
public class OpenChargeMapResponseDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("uuid")
  private String uuid;

  @JsonProperty("isRecentlyVerified")
  private boolean isRecentlyVerified;

  @JsonProperty("dateLastVerified")
  private LocalDateTime dateLastVerified;

  @JsonProperty("dateLastStatusUpdate")
  private LocalDateTime dateLastStatusUpdate;

  @JsonProperty("dateCreated")
  private LocalDateTime dateCreated;

  @JsonProperty("usageCost")
  private String usageCost;

  @JsonProperty("dataProviderID")
  private Long dataProviderID;

  @JsonProperty("numberOfPoints")
  private Long numberOfPoints;

  @JsonProperty("usageTypeID")
  private Long usageTypeID;

  @JsonProperty("submissionStatusTypeID")
  private Long submissionStatusTypeID;

  @JsonProperty("statusTypeID")
  private Long statusTypeID;

  @JsonProperty("operatorID")
  private Long operatorID;

  @JsonProperty("operatorsReference")
  private String operatorsReference;

  @JsonProperty("createdAt")
  private LocalDateTime createdAt;

  @JsonProperty("updatedAt")
  private LocalDateTime updatedAt;

  @JsonProperty("addressInfo")
  private AddressInfoDTO location;

  @JsonProperty("connections")
  private List<ConnectionDTO> connections;


    @Data
    public static class AddressInfoDTO {
      @JsonProperty("id")
      private Long id;
      @JsonProperty("title")
      private String title;
      @JsonProperty("addressLine1")
      private String addressLine1;
      @JsonProperty("town")
      private String town;
      @JsonProperty("postcode")
      private String postcode;
      @JsonProperty("stateOrProvince")
      private String stateOrProvince;
      @JsonProperty("latitude")
      private Double latitude;
      @JsonProperty("longitude")
      private Double longitude;
//      private Long countryID;
//      private String contactTelephone1;
//      private String contactTelephone2;
//      private String contactEmail;
//      private String relatedURL;
//      private String accessComments;
    }

    @Data
    public static class ConnectionDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("connectionTypeID")
    private Long connectionTypeID;

    @JsonProperty("currentTypeID")
    private Long currentTypeID;

    @JsonProperty("amps")
    private Long amps;

    @JsonProperty("voltage")
    private Long voltage;

    @JsonProperty("powerKW")
    private Long powerKW;

    @JsonProperty("quantity")
    private Long quantity;

    @JsonProperty("statusTypeID")
    private Long statusTypeID;

    @JsonProperty("statusType")
    private Long statusType;

    @JsonProperty("levelID")
    private Long levelID;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
  }
}
