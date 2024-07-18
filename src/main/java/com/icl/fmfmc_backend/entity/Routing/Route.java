package com.icl.fmfmc_backend.entity.Routing;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.*;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Data
public class Route {

  private GeometryService geometryService = new GeometryService();

  private static final double CHARGE_EFFICIENCY = 0.9; // used to calc charging time
  private static final double CHARGE_OVERHEAD =
      300; // time (s) to park, find & connect to charger etc

  private LineString originalLineStringRoute;
  private LineString workingLineStringRoute;
  private LineString finalSnappedToStopsRoute;
  private Polygon bufferedLineString;
  private String eatingOptionSearch;

  private Double routeLength;
  private Double routeDuration;

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

  private Double[] startCoordinates;
  private Double[] endCoordinates;

  private Double currentBattery;
  private Double evRange;
  private Double batteryCapacity; // kWh
  private Double minChargeLevelPct;
  private Double minChargeLevel;
  private Double chargeLevelAfterEachStopPct;
  private Double chargeLevelAfterEachStop;
  private Double finalDestinationChargeLevelPct;
  private Double finalDestinationChargeLevel;

  private List<ConnectionType> connectionTypes = new ArrayList<>();
  private List<AccessType> accessTypes = new ArrayList<>();
  private Integer minKwChargeSpeed;
  private Integer maxKwChargeSpeed;
  private Integer minNoChargePoints;

  private Boolean stopForEating = true;
  private List<FoodCategory> eatingOptions = new ArrayList<>();
  private Integer minPrice;
  private Integer maxPrice;
  private Integer maxWalkingDistance;
  private Boolean includeAlternativeEatingOptions;
  private StoppingRange stoppingRange;

  private DeviationScope chargerSearchDeviation;
  private DeviationScope eatingOptionSearchDeviation;

  // TODO: consider weather conditions in range calculation

  // Standard Constructor
  private Route(RouteRequest routeRequest) {

    this.startCoordinates = new Double[] {routeRequest.getStartLong(), routeRequest.getStartLat()};
    this.endCoordinates = new Double[] {routeRequest.getEndLong(), routeRequest.getEndLat()};

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

    this.accessTypes = routeRequest.getAccessTypes();
    this.connectionTypes = routeRequest.getConnectionTypes();
    this.minKwChargeSpeed = routeRequest.getMinKwChargeSpeed();
    this.maxKwChargeSpeed = routeRequest.getMaxKwChargeSpeed();
    this.minNoChargePoints = routeRequest.getMinNoChargePoints();

    this.stopForEating = routeRequest.getStopForEating();
    this.eatingOptions = routeRequest.getEatingOptions();
    this.minPrice = routeRequest.getMinPrice();
    this.maxPrice = routeRequest.getMaxPrice();
    this.maxWalkingDistance = routeRequest.getMaxWalkingDistance();
    this.includeAlternativeEatingOptions = routeRequest.getIncludeAlternativeEatingOptions();
    this.stoppingRange = routeRequest.getStoppingRange();

    this.chargerSearchDeviation = routeRequest.getChargerSearchDeviation();
    this.eatingOptionSearchDeviation = routeRequest.getEatingOptionSearchDeviation();
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
    this.bufferedLineString =
        GeometryService.bufferLineString(workingLineStringRoute, 0.009); // 1km
    this.routeLength = GeometryService.calculateLineStringLength(workingLineStringRoute);
    //    this.routeLength =
    //        routeResponse.getFeatures().get(0).getProperties().getSummary().getDistance();
    this.routeDuration =
        routeResponse.getFeatures().get(0).getProperties().getSummary().getDuration();
  }

  @Deprecated
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
    // updates route object routeLength and routeDuration to include detours to chargers and charge
    // time
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

  public Double[] getStoppingRangeAsFraction() {
    if (this.stoppingRange == null) {
      return new Double[] {0.33, 0.67}; // default to middle if null
    }
    return switch (this.stoppingRange) {
      case earliest -> new Double[] {0.05, 0.33};
      case early -> new Double[] {0.25, 0.5};
      case middle -> new Double[] {0.33, 0.67};
      case later -> new Double[] {0.5, 0.75};
      case latest -> new Double[] {0.67, 0.95};
      default -> new Double[] {0.33, 0.67}; // default to middle
    };
  }

  public Double getChargerSearchDeviationAsFraction() {
    if (this.chargerSearchDeviation == null) {
      return 6.5; // 6.5km
    }
    return switch (this.chargerSearchDeviation) {
      case minimal -> 3.5;
      case moderate -> 6.5;
      case significant -> 10.5;
      case extreme -> 20.5;
      default -> 6.5; // default to moderate
    };
  }

  public Double getEatingOptionSearchDeviationAsFraction() {
    if (this.eatingOptionSearchDeviation == null) {
      return 6.5; // 1.5km
    }
    return switch (this.eatingOptionSearchDeviation) {
      case minimal -> 3.5;
      case moderate -> 6.5;
      case significant -> 10.5;
      case extreme -> 20.5;
      default -> 6.5; // default to moderate
    };
  }
}
