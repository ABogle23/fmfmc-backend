package com.icl.fmfmc_backend.integration.foodEstablishment;

import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.dto.foodEstablishment.FoodEstablishmentRequest;

import java.util.List;

public interface FoodEstablishmentClient {

  /**
   * Retrieves a list of food establishments based on the provided request parameters.
   *
   * @param request the FoodEstablishmentRequest object containing the request details
   * @return a list of FoodEstablishment objects matching the request parameters
   */
  List<FoodEstablishment> getFoodEstablishmentsByParam(FoodEstablishmentRequest request);
}
