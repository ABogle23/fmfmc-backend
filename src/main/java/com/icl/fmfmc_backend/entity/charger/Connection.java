package com.icl.fmfmc_backend.entity.charger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Connection object containing individual charger connection information")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class Connection {
  private Long id;
  private String reference;
  @Schema(description = "The type of connector: Type 2 (25), CHAdeMO (2), CCS (33), Tesla (27, 30), Domestic 3-pin (3), Type 1 (1)")
  private Long connectionTypeID;

  private Long currentTypeID;
  @Schema(description = "The current of the connection in A")
  private Long amps;
  @Schema(description = "The voltage of the connection in V")
  private Long voltage;
  @Schema(description = "The power output of the connection in kW")
  private Long powerKW;
  @Schema(description = "The quantity of this connection type at this charger")
  private Long quantity;
  private Long statusTypeID;
  private Long statusType;
  @JsonIgnore
  private Long levelID;
  @JsonIgnore
  private LocalDateTime createdAt;
  @JsonIgnore
  private LocalDateTime updatedAt;
}
