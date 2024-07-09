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

  private Polygon bufferedLineString;

  private double routeLength;

  private double routeDuration;

  private List<Charger> chargersOnRoute = Collections.emptyList();

  private List<FoodEstablishment> foodEstablishments = Collections.emptyList();

  private Double currentBattery;

  private Double evRange;

  private Double minChargeLevelPct;

  private Double minChargeLevel;

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
  }

  public void addChargerToRoute(Charger charger) {
    chargersOnRoute.add(charger);
  }

  public void rechargeBattery(Double chargeLevel) {
    currentBattery = evRange * chargeLevel;
  }

}
