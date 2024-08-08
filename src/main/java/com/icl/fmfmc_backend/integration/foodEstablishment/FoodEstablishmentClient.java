package com.icl.fmfmc_backend.integration.foodEstablishment;

import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishmentRequest;

import java.util.List;

public interface FoodEstablishmentClient {

  List<FoodEstablishment> getFoodEstablishmentsByParam(FoodEstablishmentRequest request);
}
