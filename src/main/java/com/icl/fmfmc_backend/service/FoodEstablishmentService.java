package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.integration.FoodEstablishmentClient;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishmentRequest;
import com.icl.fmfmc_backend.repository.FoodEstablishmentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodEstablishmentService {
  private final FoodEstablishmentRepo foodEstablishmentRepo;
  private final FoodEstablishmentClient foodEstablishmentClient;

  public List<FoodEstablishment> getAllFoodEstablishments() {
    return foodEstablishmentRepo.findAll();
  }

  public FoodEstablishment getFoodEstablishmentById(String id) {
    Optional<FoodEstablishment> optionalFoodEstablishment = foodEstablishmentRepo.findById(id);
    if (optionalFoodEstablishment.isPresent()) {
      return optionalFoodEstablishment.get();
    }
    log.info("FoodEstablishment with id: {} doesn't exist", id);
    return null;
  }

  @Transactional
  public FoodEstablishment saveFoodEstablishment(FoodEstablishment foodEstablishment) {
    foodEstablishment.setCreatedAt(LocalDateTime.now());
    FoodEstablishment savedFoodEstablishment = foodEstablishmentRepo.save(foodEstablishment);
    log.info("FoodEstablishment with id: {} saved successfully", foodEstablishment.getId());
    return savedFoodEstablishment;
  }

  public FoodEstablishment updateFoodEstablishment(FoodEstablishment foodEstablishment) {
    Optional<FoodEstablishment> existingFoodEstablishment =
        foodEstablishmentRepo.findById(foodEstablishment.getId());
    if (existingFoodEstablishment.isPresent()) {
      FoodEstablishment updatedFoodEstablishment = foodEstablishmentRepo.save(foodEstablishment);
      log.info("FoodEstablishment with id: {} updated successfully", foodEstablishment.getId());
      return updatedFoodEstablishment;
    } else {
      log.info("FoodEstablishment with id: {} doesn't exist", foodEstablishment.getId());
      return null;
    }
  }

  public void deleteFoodEstablishmentById(String id) {
    foodEstablishmentRepo.deleteById(id);
  }

  public List<FoodEstablishment> getFoodEstablishmentsByParam(FoodEstablishmentRequest request) {
    List<FoodEstablishment> establishments =
        foodEstablishmentClient.getFoodEstablishmentsByParam(request);
    log.info("Fetched {} food establishments from FoodEstablishmentClient", establishments.size());
    return establishments;
  }
}
