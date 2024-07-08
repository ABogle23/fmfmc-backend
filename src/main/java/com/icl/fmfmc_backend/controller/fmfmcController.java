package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/charger/v1")
@RequiredArgsConstructor
@Validated
public class fmfmcController {
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;

  /**
   * This method is called when a GET request is made
   * URL: localhost:8080/charger/v1/sayhello
   * Purpose: Testing the API
   * @return A String
   */

  @GetMapping("/sayhello")
  public String sayHelloWorld() {
    return "Hello World!";
  }

  /**
   * This method is called when a GET request is made
   * URL: localhost:8080/charger/v1/
   * Purpose: Fetches all the chargers in the charger table
   * @return List of all Chargers in the charger table
   */
  @GetMapping("/")
  public ResponseEntity<List<Charger>> getAllChargers(){
    return ResponseEntity.ok().body(chargerService.getAllChargers());
  }

  /**
   * This method is called when a GET request is made
   * URL: localhost:8080/charger/v1/{id}
   * Purpose: Fetches charger with the given id
   * @param id - charger id
   * @return Charger with the given id
   */
  @GetMapping("/{id}")
  public ResponseEntity<Charger> getChargerById(@PathVariable Long id)
  {
    return ResponseEntity.ok().body(chargerService.getChargerById(id));
  }

  /**
   * This method is called when a POST request is made
   * URL: localhost:8080/charger/v1/
   * Purpose: Save a Charger entity to database
   * @param charger - Request body is a Charger entity
   * @return Saved Charger entity
   */
  @PostMapping("/")
  public ResponseEntity<Charger> saveCharger(@RequestBody Charger charger)
  {
    return ResponseEntity.ok().body(chargerService.saveCharger(charger));
  }

  /**
   * This method is called when a PUT request is made
   * URL: localhost:8080/charger/v1/
   * Purpose: Update a Charger entity
   * @param charger - Charger entity to be updated
   * @return Updated Charger entity
   */
  @PutMapping("/")
  public ResponseEntity<Charger> updateCharger(@RequestBody Charger charger)
  {
    return ResponseEntity.ok().body(chargerService.updateCharger(charger));
  }

  /**
   * This method is called when a PUT request is made
   * URL: localhost:8080/charger/v1/1 (or any other id)
   * Purpose: Delete a Charger entity
   * @param id - charger's id to be deleted
   * @return a String message indicating charger record has been deleted successfully
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteChargerById(@PathVariable Long id)
  {
    chargerService.deleteChargerById(id);
    return ResponseEntity.ok().body("Deleted charger successfully");
  }


  /**
   * This method is called when a GET request is made
   * URL: localhost:8080/charger/v1/food-establishment
   * Purpose: Fetches all FoodEstablishments in database
   * @return List of FoodEstablishments
   */
  @GetMapping("/food-establishment")
  public ResponseEntity<List<FoodEstablishment>> getAllFoodEstablishments(){
    return ResponseEntity.ok().body(foodEstablishmentService.getAllFoodEstablishments());
  }


}
