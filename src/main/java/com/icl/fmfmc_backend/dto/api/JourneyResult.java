package com.icl.fmfmc_backend.dto.api;

import com.icl.fmfmc_backend.geometry_service.PolylineUtility;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.routing.Journey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Journey result containing journey details")
public class JourneyResult {

  // TODO: create constructor that takes Route object as argument
  // TODO: including battery levels before and after stops and end

  private String originalRoutePolyline;
  private String finalRoutePolyline;
  private String chargerPolygon;
  private String eatingOptionPolygon;
  private List<String> eatingSearchCircles = new ArrayList<>();
  private Double distance;
  private Double time;
  private Journey.SegmentDetails segmentDetails;
  private List<Charger> chargers;
  private List<FoodEstablishment> foodEstablishments;
  private JourneyRequest context; // original request


  public JourneyResult(Journey journey, JourneyRequest context) {
    this.originalRoutePolyline = PolylineUtility.encodeLineString(journey.getOriginalLineStringRoute());
    this.finalRoutePolyline = PolylineUtility.encodeLineString(journey.getFinalSnappedToStopsRoute());
    this.chargerPolygon = PolylineUtility.encodePolygon(journey.getBufferedLineString());
    this.eatingOptionPolygon = journey.getEatingOptionSearch();
    for (int i = 0; i < journey.getEatingSearchCircles().size(); i++) {
      this.eatingSearchCircles.add(PolylineUtility.encodePolygon(journey.getEatingSearchCircles().get(i)));
    }
    this.distance = journey.getRouteLength();
    this.time = journey.getRouteDuration();
    this.segmentDetails = journey.getSegmentDetails();
    this.chargers = journey.getChargersOnRoute();
    this.foodEstablishments = journey.getFoodEstablishments();
    this.context = context;
  }

}
