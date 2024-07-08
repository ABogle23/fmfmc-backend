package com.icl.fmfmc_backend.entity.FoodEstablishment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class FoursquareResponse {
    @JsonProperty("results")
    private List<FoodEstablishment> results;

}