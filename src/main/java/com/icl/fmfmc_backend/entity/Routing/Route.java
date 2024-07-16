package com.icl.fmfmc_backend.entity.Routing;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Data
public class Route {

  private GeometryService geometryService = new GeometryService();

  private static final double CHARGE_EFFICIENCY = 0.9; // used to calc charging time
  private static final double CHARGE_OVERHEAD = 300; // time (s) to park, find & connect to charger etc


  private LineString originalLineStringRoute;
  private LineString workingLineStringRoute;
  private LineString finalSnappedToStopsRoute;
  private Polygon bufferedLineString;
  private String eatingOptionSearch;

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

    private void clearSegmentDurationAndDistance() {
      segmentDurations.clear();
      segmentDistances.clear();
    }

    public void addStop(Double duration) {
      stopDurations.add(duration);
    }
  }

  private List<Charger> chargersOnRoute = new ArrayList<>();
  private List<FoodEstablishment> foodEstablishments = new ArrayList<>();
  private Charger foodAdjacentCharger = null; // must be stopped at
  private Boolean foodAdjacentChargerUsed = false;

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
    this.originalLineStringRoute =
        geometryService.createLineString(
            routeResponse.getFeatures().get(0).getGeometry().getCoordinates());
    this.workingLineStringRoute = originalLineStringRoute;
    this.bufferedLineString = GeometryService.bufferLineString(workingLineStringRoute, 0.009); // 1km
    this.routeLength = GeometryService.calculateLineStringLength(workingLineStringRoute);
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

    segmentDetails.addStop(chargeTime); // add charge time to segment details
  }

  public void setDurationsAndDistances(OSRDirectionsServiceGeoJSONResponse response) {

    // reset segments (except stops) resulting from previous function calls.
    segmentDetails.clearSegmentDurationAndDistance();
    // set individual distances and durations per leg.
    for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO feature : response.getFeatures()) {
      for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO.PropertiesDTO.SegmentDTO segment :
          feature.getProperties().getSegments()) {
        segmentDetails.addSegment(segment.getDuration(), segment.getDistance());
      }
    }
  }

  public void setTotalDurationAndDistance(OSRDirectionsServiceGeoJSONResponse response) {
    // updates route object routeLength and routeDuration to include detours to chargers and charge time
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
