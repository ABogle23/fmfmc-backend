package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.Api.ElectricVehicleDto;
import com.icl.fmfmc_backend.dto.ElectricVehicle;
import com.icl.fmfmc_backend.service.ElectricVehicleService;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class ElectricVehicleController {
    @Autowired
    private ElectricVehicleService vehicleService;

    @LogExecutionTime(message = "Returning Vehicles in")
    @GetMapping("/all")
    public ResponseEntity<List<ElectricVehicleDto>> getAllVehicles() {
        List<ElectricVehicleDto> vehicles = vehicleService.findAllVehiclesCompact();
        return ResponseEntity.ok(vehicles);
    }

}