package com.icl.fmfmc_backend.entity.Routing;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.service.ChargerService;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Route {

  private GeometryService geometryService = new GeometryService();

  private static final double CHARGE_EFFICIENCY = 0.9; // used to calc charging time
  private static final double CHARGE_OVERHEAD = 300; // time (s) to park, find & connect to charger etc


  private LineString lineStringRoute;
  private LineString routeSnappedToStops;
  private Polygon bufferedLineString;

  private Double routeLength;
  private Double routeDuration;
  //  private List<Double> segmentsDurations = Collections.emptyList();
  //  private List<Double> segmentsDistances = Collections.emptyList();
  //  private List<Double> chargeDurations = Collections.emptyList();

  private SegmentDetails segmentDetails = new SegmentDetails();

  @Data
  public static class SegmentDetails {
    private List<Double> stopDurations = new ArrayList<>();
    private List<Double> segmentDurations = new ArrayList<>();
    private List<Double> segmentDistances = new ArrayList<>();

    public void addSegment(Double duration, Double distance) {
      segmentDurations.add(duration);
      segmentDistances.add(distance);
    }

    public void addStop(Double duration) {
      stopDurations.add(duration);
    }
  }

  private List<Charger> chargersOnRoute = new ArrayList<>();
  private List<FoodEstablishment> foodEstablishments = new ArrayList<>();

  private List<ConnectionType> connectionTypes = new ArrayList<>();

  private Double currentBattery;
  private Double evRange;
  private Double batteryCapacity; // kWh
  private Double minChargeLevelPct;
  private Double minChargeLevel;
  private Double chargeLevelAfterEachStopPct;
  private Double chargeLevelAfterEachStop;
  private Double finalDestinationChargeLevelPct;
  private Double finalDestinationChargeLevel;

  // TODO: consider weather conditions in range calculation

  // Standard Constructor

  private Route(RouteRequest routeRequest) {
    this.connectionTypes = routeRequest.getConnectionTypes();
    this.currentBattery = routeRequest.getStartingBattery() * routeRequest.getEvRange();
    this.evRange = routeRequest.getEvRange();
    this.minChargeLevelPct = routeRequest.getMinChargeLevel();
    this.minChargeLevel = routeRequest.getMinChargeLevel() * routeRequest.getEvRange();
    this.chargeLevelAfterEachStopPct = routeRequest.getChargeLevelAfterEachStop();
    this.chargeLevelAfterEachStop =
        routeRequest.getChargeLevelAfterEachStop() * routeRequest.getEvRange();
    this.finalDestinationChargeLevelPct = routeRequest.getFinalDestinationChargeLevel();
    this.finalDestinationChargeLevel =
        routeRequest.getFinalDestinationChargeLevel() * routeRequest.getEvRange();

    if (routeRequest.getBatteryCapacity() != null) {
      this.batteryCapacity = routeRequest.getBatteryCapacity();
    } else {
      this.batteryCapacity = (routeRequest.getEvRange() * 200) / 1000000; // guesstimate
    }
    System.out.println("Battery capacity: " + this.batteryCapacity);
  }

  // Standard Constructor
  public Route(
      LineString route,
      Polygon bufferedLineString,
      Double routeLength,
      Double routeDuration,
      RouteRequest routeRequest) {
    this(routeRequest);
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
    this.finalDestinationChargeLevelPct = routeRequest.getFinalDestinationChargeLevel();
    this.finalDestinationChargeLevel =
        routeRequest.getFinalDestinationChargeLevel() * routeRequest.getEvRange();

    if (routeRequest.getBatteryCapacity() != null) {
      this.batteryCapacity = routeRequest.getBatteryCapacity();
    } else {
      this.batteryCapacity = (routeRequest.getEvRange() * 200) / 1000000; // guesstimate
    }
    System.out.println("Battery capacity: " + this.batteryCapacity);
  }

  // Secondary Constructor coupled to OSRDirectionsServiceGeoJSONResponse

  public Route(OSRDirectionsServiceGeoJSONResponse routeResponse, RouteRequest routeRequest) {
    this(routeRequest);
    this.lineStringRoute =
        geometryService.createLineString(
            routeResponse.getFeatures().get(0).getGeometry().getCoordinates());
    this.bufferedLineString = GeometryService.bufferLineString(lineStringRoute, 0.009);
    this.routeLength = GeometryService.calculateLineStringLength(lineStringRoute);
//    this.routeLength =
//        routeResponse.getFeatures().get(0).getProperties().getSummary().getDistance();
    this.routeDuration =
        routeResponse.getFeatures().get(0).getProperties().getSummary().getDuration();
  }

  public void addChargerToRoute(Charger charger) {
    chargersOnRoute.add(charger);
  }

  public void rechargeBattery(Double chargeSpeed) {
    addBatteryChargeTime(chargeSpeed);
    currentBattery = evRange * chargeLevelAfterEachStopPct;
  }

  public void addBatteryChargeTime(Double chargeSpeedkw) {
    Double rechargePct = ((chargeLevelAfterEachStop - currentBattery) / evRange);
    Double chargeTime = (batteryCapacity * rechargePct) / (chargeSpeedkw * CHARGE_EFFICIENCY);
    chargeTime = chargeTime * 3600.0; // convert to seconds
    chargeTime += CHARGE_OVERHEAD; // add charge overhead
    chargeTime = Math.round(chargeTime * 10) / 10.0; // round to 1dp
    segmentDetails.addStop(chargeTime);
  }

  public void setDurationsAndDistances(OSRDirectionsServiceGeoJSONResponse response) {
    // Assuming geometryService and other members are initialized elsewhere
    for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO feature : response.getFeatures()) {
      for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO.PropertiesDTO.SegmentDTO segment :
          feature.getProperties().getSegments()) {
        segmentDetails.addSegment(segment.getDuration(), segment.getDistance());
      }
    }
  }

  public void setTotalDurationAndDistance(OSRDirectionsServiceGeoJSONResponse response) {
    // Assuming geometryService and other members are initialized elsewhere
    Double totalDuration = 0.0;
    Double totalDistance = 0.0;

    totalDistance += response.getFeatures().get(0).getProperties().getSummary().getDistance();
    totalDuration += response.getFeatures().get(0).getProperties().getSummary().getDuration();

    for (Double duration : segmentDetails.getStopDurations()) {
      totalDuration += duration;
    }

    setRouteLength(totalDistance);
    setRouteDuration(totalDuration);

  }
}
