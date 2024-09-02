package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.api.ElectricVehicleDto;
import com.icl.fmfmc_backend.service.ElectricVehicleService;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST controller for managing electric vehicles. */
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:63342")
public class ElectricVehicleController {
  @Autowired private ElectricVehicleService vehicleService;

  /**
   * Endpoint to get all electric vehicles.
   *
   * @return ResponseEntity containing a list of ElectricVehicleDto objects
   */
  @LogExecutionTime(message = "Returning Vehicles in")
  @GetMapping("/all")
  public ResponseEntity<List<ElectricVehicleDto>> getAllVehicles() {
    List<ElectricVehicleDto> vehicles = vehicleService.findAllVehiclesCompact();
    return ResponseEntity.ok(vehicles);
  }
}
