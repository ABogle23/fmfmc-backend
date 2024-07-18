package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishmentRequest;

import java.util.List;

public interface FoodEstablishmentClient {

  List<FoodEstablishment> getFoodEstablishmentsByParam(FoodEstablishmentRequest request);
}
