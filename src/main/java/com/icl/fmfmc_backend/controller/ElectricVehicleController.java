package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.api.ApiErrorResponse;
import com.icl.fmfmc_backend.dto.api.ChargerRequest;
import com.icl.fmfmc_backend.dto.api.ElectricVehicleDto;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.service.ElectricVehicleService;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Operation(
      summary = "Get all Electric Vehicles",
      description = "Returns a list of all electric vehicles.",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = ElectricVehicleDto.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description =
                "Bad Request - The request could not be understood and therefore could not be processed",
            content = @Content)
      })
  @LogExecutionTime(message = "Returning Vehicles in")
  @DocsResponseCodes
  @GetMapping("/all")
  public ResponseEntity<List<ElectricVehicleDto>> getAllVehicles() {
    List<ElectricVehicleDto> vehicles = vehicleService.findAllVehiclesCompact();
    return ResponseEntity.ok(vehicles);
  }
}
