package com.icl.fmfmc_backend.entity.Charger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
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
  private GeoCoordinates geocodes;
  private Long dataProviderID;
  private Long numberOfPoints;
  private Long usageTypeID;
  private Long submissionStatusTypeID;
  private Long statusTypeID;
  private Long operatorID;
  private String operatorsReference;
  private LocalDateTime createdAt;
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
