package com.icl.fmfmc_backend.dto.pointOfInterest;

import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@Data
public class PointOfInterest {
  private List<FoodCategory> eatingOptions;
  private Integer minPrice;
  private Integer maxPrice;
  private Integer maxWalkingDistance;
  private List<FoodEstablishment> foodEstablishments = new ArrayList<>();


    public PointOfInterest(JourneyRequest journeyRequest) {
        this.eatingOptions = journeyRequest.getEatingOptions();
        this.minPrice = journeyRequest.getMinPrice();
        this.maxPrice = journeyRequest.getMaxPrice();
        this.maxWalkingDistance = journeyRequest.getMaxWalkingDistance();
    }
}
