package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
@RequiredArgsConstructor
@Validated
@Hidden
public class ChargerController {
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;

  /**
   * This method is called when a GET request is made URL: localhost:8080/charger/v1/sayhello
   * Purpose: Testing the API
   *
   * @return A String
   */
  @GetMapping("/sayhello")
  public String sayHelloWorld() {
    return "Hello World!";
  }

  /**
   * This method is called when a GET request is made URL: localhost:8080/charger/v1/ Purpose:
   * Fetches all the chargers in the charger table
   *
   * @return List of all Chargers in the charger table
   */
  @GetMapping("/")
  public ResponseEntity<List<Charger>> getAllChargers() {
    return ResponseEntity.ok().body(chargerService.getAllChargers());
  }

  /**
   * This method is called when a GET request is made URL: localhost:8080/charger/v1/{id} Purpose:
   * Fetches charger with the given id
   *
   * @param id - charger id
   * @return Charger with the given id
   */
  @GetMapping("/{id}")
  public ResponseEntity<Charger> getChargerById(@PathVariable Long id) {
    return ResponseEntity.ok().body(chargerService.getChargerById(id));
  }

  //TODO: allow for filtering by additional params beyond bounding box
  @GetMapping("/all-chargers")
  public ResponseEntity<List<Charger>> getAllChargersWithinBoundingBox(
      @RequestParam(required = false) Double topLeftLat,
      @RequestParam(required = false) Double topLeftLong,
      @RequestParam(required = false) Double bottomRightLat,
      @RequestParam(required = false) Double bottomRightLong) {
    ChargerQuery chargerQuery =
        ChargerQuery.builder()
            .topLeftLatLng(topLeftLat, topLeftLong)
            .bottomRightLatLng(bottomRightLat, bottomRightLong)
            .build();

    return ResponseEntity.ok().body(chargerService.getChargersByParams(chargerQuery));

  }
}
