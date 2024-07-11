package com.icl.fmfmc_backend.dto.Api;

import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

  private String routePolyline;

  private String chargerPolygon;

  private String eatingOptionPolygon;

  private Double distance;

  private Double time; // mins

  private List<Charger> chargers;

  private List<FoodEstablishment> foodEstablishments;

  private RouteRequest context; // original request
}
