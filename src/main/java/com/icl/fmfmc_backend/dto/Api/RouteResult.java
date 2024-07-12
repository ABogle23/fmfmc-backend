package com.icl.fmfmc_backend.dto.Api;

import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

  // TODO: create constructor that takes Route object as argument
  // TODO: including battery levels before and after stops and end

  private String routePolyline;
  private String chargerPolygon;
  private String eatingOptionPolygon;
  private Double distance;
  private Double time;
  private Route.SegmentDetails segmentDetails;
  private List<Charger> chargers;
  private List<FoodEstablishment> foodEstablishments;
  private RouteRequest context; // original request

}
