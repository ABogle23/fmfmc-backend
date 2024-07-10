package com.icl.fmfmc_backend.entity.Routing;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.Collections;
import java.util.List;

@Data
public class Route {

  private LineString lineStringRoute;

  private LineString routeSnappedToStops;

  private Polygon bufferedLineString;

  private double routeLength;

  private double routeDuration;

  private List<Charger> chargersOnRoute = Collections.emptyList();

  private List<FoodEstablishment> foodEstablishments = Collections.emptyList();

  private Double currentBattery;

  private Double evRange;

  private Double batteryCapacity; // kWh

  private Double minChargeLevelPct;

  private Double minChargeLevel;

  private Double chargeLevelAfterEachStopPct;

  private Double chargeLevelAfterEachStop;

  private Double chargeLevelAtDestination;


  // TODO: consider weather conditions in range calculation
  // TODO: detailed segment information with distance and timings
  // TODO: expand duration calculation to include charging time
  public Route(
      LineString route,
      Polygon bufferedLineString,
      Double routeLength,
      Double routeDuration,
      RouteRequest routeRequest) {
    this.lineStringRoute = route;
    this.bufferedLineString = bufferedLineString;
    this.routeLength = routeLength;
    this.routeDuration = routeDuration;
    this.currentBattery = routeRequest.getStartingBattery() * routeRequest.getEvRange();
    this.evRange = routeRequest.getEvRange();
    this.minChargeLevelPct = routeRequest.getMinChargeLevel();
    this.minChargeLevel = routeRequest.getMinChargeLevel() * routeRequest.getEvRange();
    this.chargeLevelAfterEachStopPct = routeRequest.getChargeLevelAfterEachStop();
    this.chargeLevelAfterEachStop =
        routeRequest.getChargeLevelAfterEachStop() * routeRequest.getEvRange();

    if (routeRequest.getBatteryCapacity() != null) {
      this.batteryCapacity = routeRequest.getBatteryCapacity();
    }
    else {
        this.batteryCapacity = (routeRequest.getEvRange() * 200) / 1000000; // guesstimate
    }
    System.out.println("Battery capacity: " + this.batteryCapacity);

  }

  public void addChargerToRoute(Charger charger) {
    chargersOnRoute.add(charger);
  }

  public void rechargeBattery() {
    currentBattery = evRange * chargeLevelAfterEachStopPct;
  }
}
