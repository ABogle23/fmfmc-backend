package com.icl.fmfmc_backend.entity.charger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Charger object containing charger information")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "chargers", indexes = {
        @Index(name = "idx_number_of_points", columnList = "numberOfPoints"),
        @Index(name = "idx_location", columnList = "location")
})
public class Charger {

  @Id private Long id;

  private String uuid;

  private String title;

  @JsonProperty("isRecentlyVerified")
  private boolean isRecentlyVerified;
  private LocalDateTime dateLastVerified;
  private LocalDateTime dateLastStatusUpdate;
  private LocalDateTime dateCreated;
  private String usageCost;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "address_info_id", referencedColumnName = "id")
  private AddressInfo addressInfo;

  @ElementCollection(fetch = FetchType.LAZY)
  private List<Connection> connections;

  @Embedded
  @Schema(description = "Geographical coordinates of the charger")
  private GeoCoordinates geocodes;
  private Long dataProviderID;
  @Schema(description = "Number of individual charging bays for an EV, distinct from number of charging connectors")
  private Long numberOfPoints;
  @Schema(description = "Type of access to the charger: Public (1, 4, 5, 7), Restricted (6), Private (2, 3)")
  private Long usageTypeID;
  private Long submissionStatusTypeID;
  private Long statusTypeID;
  private Long operatorID;
  private String operatorsReference;
  @JsonIgnore
  private LocalDateTime createdAt;
  @JsonIgnore
  private LocalDateTime updatedAt;

  @JsonIgnore
  @Column(columnDefinition = "Point", nullable = false)
  private Point location;

//  @PrePersist
//  @PreUpdate
//  private void ensureNonNullNumberOfPoints() {
//    if (numberOfPoints == null) {
//      numberOfPoints = 1L;
//    }
//  }

}
