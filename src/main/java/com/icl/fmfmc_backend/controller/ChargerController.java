package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.api.ChargerRequest;
import com.icl.fmfmc_backend.dto.charger.ChargerCompactDTO;
import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing chargers.
 */
@RestController
@RequestMapping("/api/charger")
@RequiredArgsConstructor
@Validated
public class ChargerController {
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;

  /**
   * This method is called when a GET request is made to: /api/charger/sayhello
   * Purpose: Testing the API
   *
   * @return A String
   */
  @Hidden
  @GetMapping("/sayhello")
  public String sayHelloWorld() {
    return "Hello World!";
  }

  /**
   * This method is called when a GET request is made to: /api/charger/all
   * Purpose: Fetches all the chargers in the charger table
   *
   * @return List of all Chargers in the charger table
   */
  @GetMapping("/all")
  public ResponseEntity<List<Charger>> getAllChargers() {
    return ResponseEntity.ok().body(chargerService.getAllChargers());
  }

  /**
   * This method is called when a GET request is made to: /api/charger/{id}
   * Purpose: Fetches charger with the given id
   *
   * @param id - charger id
   * @return Charger with the given id
   */
  @GetMapping("/{id}")
  public ResponseEntity<Charger> getChargerById(@PathVariable Long id) {
    return ResponseEntity.ok().body(chargerService.getChargerById(id));
  }

  /**
   * This method is called when a POST request is made to: /api/charger/chargers
   * Purpose: Get chargers within a bounding box
   *
   * @param chargerRequest - Charger request object containing parameters
   * @return List of chargers within the bounding box and additional parameters
   */
  @Operation(
      summary = "Get chargers within a bounding box",
      description =
          "Returns a list of chargers within a given bounding box and additional parameters",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Charger request object containing parameters",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ChargerRequest.class))),
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Charger.class))))
      })
  @PostMapping("/chargers")
  public ResponseEntity<List<?>> getChargersByParams(
      @Valid @RequestBody ChargerRequest chargerRequest) {

    ChargerQuery chargerQuery =
        ChargerQuery.builder()
            .topLeftLatLng(chargerRequest.getTopLeftLat(), chargerRequest.getTopLeftLong())
            .bottomRightLatLng(
                chargerRequest.getBottomRightLat(), chargerRequest.getBottomRightLong())
            .minKwChargeSpeed(chargerRequest.getMinKwChargeSpeed())
            .maxKwChargeSpeed(chargerRequest.getMaxKwChargeSpeed())
            .accessTypeIds(chargerRequest.getAccessTypes())
            .connectionTypeIds(chargerRequest.getConnectionTypes())
            .minNoChargePoints(chargerRequest.getMinNoChargePoints())
            .build();

    List<Charger> result = chargerService.getChargersByParams(chargerQuery);

    if (chargerRequest.getCompact()) {
      List<ChargerCompactDTO> compactResult =
          result.stream()
              .map(charger -> new ChargerCompactDTO(charger.getId(), charger.getGeocodes()))
              .collect(Collectors.toList());
      return ResponseEntity.ok().body(compactResult);
    }

    return ResponseEntity.ok().body(result);
  }
}
