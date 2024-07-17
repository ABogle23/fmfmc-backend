package com.icl.fmfmc_backend.dto.Api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElectricVehicleDto {
  private Long id;
  private String brand;
  private String model;
  private Double evRange;
  private Double batteryCapacity;

}
