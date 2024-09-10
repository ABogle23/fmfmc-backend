package com.icl.fmfmc_backend.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Electric Vehicle object containing electric vehicle information")
@Data
@AllArgsConstructor
public class ElectricVehicleDto {
  private Long id;
  private String brand;
  private String model;
  private Double evRange;
  private Double batteryCapacity;

}
