package com.icl.fmfmc_backend.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "chargers")
public class Charger {

  @Id private Long id;

  private String uuid;

  @JsonProperty("isRecentlyVerified")
  private boolean isRecentlyVerified;

  //  @JsonProperty("date_last_verified")
  private LocalDateTime dateLastVerified;

  //  @JsonProperty("date_last_status_update")
  private LocalDateTime dateLastStatusUpdate;

  //  @JsonProperty("date_created")
  private LocalDateTime dateCreated;

  //  @JsonProperty("usage_cost")
  private String usageCost;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "address_info_id", referencedColumnName = "id")
  private AddressInfo addressInfo;

  @ElementCollection(fetch = FetchType.LAZY)
  private List<Connection> connections;

  @Embedded
  private GeoCoordinates geocodes;

  //  @JsonProperty("data_provider_id")
  private Long dataProviderID;

  //  @JsonProperty("number_of_points")
  private Long numberOfPoints;

  //  @JsonProperty("usage_type_id")
  private Long usageTypeID;

  //  @JsonProperty("submission_status_type_id")
  private Long submissionStatusTypeID;

  //  @JsonProperty("status_type_id")
  private Long statusTypeID;

  //  @JsonProperty("operator_id")
  private Long operatorID;

  //  @JsonProperty("operators_reference")
  private String operatorsReference;

  //  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  //  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  @Column(columnDefinition = "Point")
  private Point location;

//  @PrePersist
//  @PreUpdate
//  private void ensureNonNullNumberOfPoints() {
//    if (numberOfPoints == null) {
//      numberOfPoints = 1L;
//    }
//  }

}
