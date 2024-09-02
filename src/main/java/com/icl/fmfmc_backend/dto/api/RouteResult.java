package com.icl.fmfmc_backend.dto.api;

import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.routing.Route;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Route result containing journey details")
public class RouteResult {

  // TODO: create constructor that takes Route object as argument
  // TODO: including battery levels before and after stops and end

  private String originalRoutePolyline;
  private String finalRoutePolyline;
  private String chargerPolygon;
  private String eatingOptionPolygon;
  private List<String> eatingSearchCircles = new ArrayList<>();
  private Double distance;
  private Double time;
  private Route.SegmentDetails segmentDetails;
  private List<Charger> chargers;
  private List<FoodEstablishment> foodEstablishments;
  private RouteRequest context; // original request


  public RouteResult(Route route, RouteRequest context) {
    this.originalRoutePolyline = PolylineUtility.encodeLineString(route.getOriginalLineStringRoute());
    this.finalRoutePolyline = PolylineUtility.encodeLineString(route.getFinalSnappedToStopsRoute());
    this.chargerPolygon = PolylineUtility.encodePolygon(route.getBufferedLineString());
    this.eatingOptionPolygon = route.getEatingOptionSearch();
    for (int i = 0; i < route.getEatingSearchCircles().size(); i++) {
      this.eatingSearchCircles.add(PolylineUtility.encodePolygon(route.getEatingSearchCircles().get(i)));
    }
    this.distance = route.getRouteLength();
    this.time = route.getRouteDuration();
    this.segmentDetails = route.getSegmentDetails();
    this.chargers = route.getChargersOnRoute();
    this.foodEstablishments = route.getFoodEstablishments();
    this.context = context;
  }

}
