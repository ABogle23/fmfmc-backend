package com.icl.fmfmc_backend.dto.foodEstablishment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import lombok.Getter;

import java.util.List;

@Getter
public class FoursquareResponse {
    @JsonProperty("results")
    private List<FoodEstablishment> results;

}