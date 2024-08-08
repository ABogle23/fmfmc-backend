package com.icl.fmfmc_backend.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MapboxRequest {

    private MapboxRequest() {
        //    this.alternativeRoutes = new AlternativeRoutes(1);
//        this.attributes = List.of("avgspeed", "detourfactor", "percentage");
//        this.geometrySimplify = "true";
//        this.preference = "recommended";
//        this.units = "m";
    }

    // additional constructor that allows overriding default values
    public MapboxRequest(List<Double[]> coordinates) {
        this(); // call the default constructor to set defaults
        this.coordinates = coordinates; // override coordinates
    }

    @JsonProperty("coordinates")
    private List<Double[]> coordinates;



}
