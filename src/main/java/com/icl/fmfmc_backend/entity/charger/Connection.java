package com.icl.fmfmc_backend.entity.charger;

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
  private Long connectionTypeID;
  private Long currentTypeID;
  private Long amps;
  private Long voltage;
  private Long powerKW;
  private Long quantity;
  private Long statusTypeID;
  private Long statusType;
  private Long levelID;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
