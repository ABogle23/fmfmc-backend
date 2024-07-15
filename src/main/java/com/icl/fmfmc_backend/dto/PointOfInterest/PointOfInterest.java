package com.icl.fmfmc_backend.dto.PointOfInterest;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PointOfInterest {
  private List<FoodCategory> eatingOptions;
  private Integer minPrice;
  private Integer maxPrice;
  private Integer maxWalkingDistance;
  private List<FoodEstablishment> foodEstablishments = new ArrayList<>();


    public PointOfInterest(RouteRequest routeRequest) {
        this.eatingOptions = routeRequest.getEatingOptions();
        this.minPrice = routeRequest.getMinPrice();
        this.maxPrice = routeRequest.getMaxPrice();
        this.maxWalkingDistance = routeRequest.getMaxWalkingDistance();
    }
}
