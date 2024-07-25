package com.icl.fmfmc_backend.entity.Routing;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.*;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Route {

  private static final GeometryService geometryService = new GeometryService();

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
    private List<Double> arrivalCharges = new ArrayList<>();
    private List<Double> departingCharges = new ArrayList<>();
    private List<Double> chargeSpeedKw = new ArrayList<>();

    public void addSegment(Double duration, Double distance) {
      segmentDurations.add(duration);
      segmentDistances.add(distance);
    }

    private void clearSegmentDurationAndDistance() {
      segmentDurations.clear();
      segmentDistances.clear();
    }

    public void addStopDuration(Double duration) {
      stopDurations.add(duration);
    }

    public void addChargeTime(Double arrivalCharge, Double departingCharge) {
      arrivalCharges.add(arrivalCharge);
      departingCharges.add(departingCharge);
    }

    public void addChargeSpeed(Double chargeSpeed) {
      chargeSpeedKw.add(chargeSpeed);
    }

  }

  private List<Charger> chargersOnRoute = new ArrayList<>();
  private List<FoodEstablishment> foodEstablishments = new ArrayList<>();
  private Charger foodAdjacentCharger = null; // must be stopped at
  private Boolean foodAdjacentChargerUsed = false;

  private Double[] startCoordinates;
  private Double[] endCoordinates;

  private Double StartBattery;
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

    this.StartBattery = routeRequest.getStartingBattery();
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

  // Secondary Constructor using DirectionsResponse and RouteRequest

  public Route(DirectionsResponse directionsResponse, RouteRequest routeRequest) {
    this(routeRequest);
    this.originalLineStringRoute = directionsResponse.getLineString();
    this.workingLineStringRoute = originalLineStringRoute;
    this.bufferedLineString =
        GeometryService.bufferLineString(workingLineStringRoute, 0.009); // 1km
    this.routeLength = GeometryService.calculateLineStringLength(workingLineStringRoute);
    this.routeDuration = directionsResponse.getTotalDuration();
  }

  // Secondary Constructor coupled to OSRDirectionsServiceGeoJSONResponse

  @Deprecated
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
    addArrivalAndDepartureCharges();
    currentBattery = evRange * chargeLevelAfterEachStopPct;
  }

  public void reduceBattery(Double distance) {
    currentBattery -= distance;
  }

  public void addBatteryChargeTime(Double chargeSpeedkw) {
    Double rechargePct = ((chargeLevelAfterEachStop - currentBattery) / evRange);
    Double chargeTime = (batteryCapacity * rechargePct) / (chargeSpeedkw * CHARGE_EFFICIENCY);
    chargeTime = chargeTime * 3600.0; // convert to seconds
    chargeTime += CHARGE_OVERHEAD; // add charge overhead
    chargeTime = Math.round(chargeTime * 10) / 10.0; // round to 1dp

    segmentDetails.addStopDuration(chargeTime); // add charge time to segment details
    segmentDetails.addChargeSpeed(chargeSpeedkw); // add charge speed to segment details
  }

  public void addArrivalAndDepartureCharges() {
    Double arrivalCharge = currentBattery / evRange;
    Double departingCharge = chargeLevelAfterEachStopPct;
    segmentDetails.addChargeTime(arrivalCharge, departingCharge);
  }

  public void setDurationsAndDistances(DirectionsResponse response) {

    // reset segments (except stops) resulting from previous function calls.
    segmentDetails.clearSegmentDurationAndDistance();
    // set individual distances and durations per leg.
    segmentDetails.segmentDurations = response.getLegDurations();
    segmentDetails.segmentDistances = response.getLegDistances();
  }

  public void setTotalDurationAndDistance(DirectionsResponse response) {
    // updates route object routeLength and routeDuration to include detours to chargers and charge
    // time
    Double totalDuration = 0.0;
    Double totalDistance = 0.0;

    totalDistance += response.getTotalDistance();
    totalDuration += response.getTotalDuration();

    for (Double duration : segmentDetails.getStopDurations()) {
      totalDuration += duration;
    }

    setRouteLength(totalDistance);
    setRouteDuration(totalDuration);
  }

  @Deprecated // don't use, coupled to OSRDirectionsServiceGeoJSONResponse
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

  @Deprecated // don't use, coupled to OSRDirectionsServiceGeoJSONResponse
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
      case extendedEarly -> new Double[] {0.05, 0.6};
      case extendedMiddle -> new Double[] {0.2, 0.8};
      case extendedLater -> new Double[] {0.4, 0.95};
      default -> new Double[] {0.33, 0.67}; // default to middle
    };
  }

  public void expandStoppingRange() {
    switch (this.stoppingRange) {
      case earliest -> this.stoppingRange = StoppingRange.early;
      case early -> this.stoppingRange = StoppingRange.middle;
      case middle -> this.stoppingRange = StoppingRange.later;
      case later -> this.stoppingRange = StoppingRange.latest;
      case latest -> this.stoppingRange = StoppingRange.extendedEarly;
      case extendedEarly -> this.stoppingRange = StoppingRange.extendedMiddle;
      case extendedMiddle -> this.stoppingRange = StoppingRange.extendedLater;
      case extendedLater -> {
        return;
      }
    }
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

  public Boolean expandEatingOptionSearchDeviation() {
    // TODO: consider limiting expansion based on route length
    switch (this.eatingOptionSearchDeviation) {
      case minimal:
        this.eatingOptionSearchDeviation = DeviationScope.moderate;
        break;
      case moderate:
        this.eatingOptionSearchDeviation = DeviationScope.significant;
        break;
      case significant:
        this.eatingOptionSearchDeviation = DeviationScope.extreme;
        break;
      case extreme:
        return false;
    }
    return true;
  }

  public void expandChargerSearchDeviation() {
    switch (this.chargerSearchDeviation) {
      case minimal -> this.chargerSearchDeviation = DeviationScope.moderate;
      case moderate -> this.chargerSearchDeviation = DeviationScope.significant;
      case significant -> this.chargerSearchDeviation = DeviationScope.extreme;
      case extreme -> {
        return;
      }
    }
  }

  public void maximiseEatingOptionSearchDeviation() {
    this.eatingOptionSearchDeviation = DeviationScope.extreme;
  }

  public void expandFoodCategorySearch() {
    Set<FoodCategory> expandedEatingOptions = new HashSet<>();

    for (FoodCategory childCategory : this.eatingOptions) {
      if (childCategory.getParent() != null) {
        expandedEatingOptions.add(childCategory.getParent());
      } else {
        expandedEatingOptions.add(childCategory);
      }
    }
    this.eatingOptions = new ArrayList<>(expandedEatingOptions);
  }

  public void expandPriceRange() {
    if (minPrice != null && minPrice > 1) {
      minPrice -= 1;
    }
    if (maxPrice != null && maxPrice < 4) {
      maxPrice += 1;
    }
  }

  public void expandChargeSpeedRange() {
    if (minKwChargeSpeed != null && minKwChargeSpeed > 7) {
      minKwChargeSpeed = 7;
    }
    if (maxKwChargeSpeed != null && maxKwChargeSpeed < 350) {
      maxKwChargeSpeed = 350;
    }
  }

  public void relaxChargingRange() {
    if (minChargeLevelPct != null && minChargeLevelPct > 0.1) {
      minChargeLevelPct = 0.1;
      minChargeLevel = 0.1 * evRange;
    }
    if (chargeLevelAfterEachStopPct != null && chargeLevelAfterEachStopPct < 0.9) {
      chargeLevelAfterEachStopPct = 1.0;
      chargeLevelAfterEachStop = evRange;
    }
  }

  public void clearChargersOnRoute() {
    chargersOnRoute.clear();
  }

  public void resetBatteryLevel() {
    currentBattery = StartBattery * evRange;
  }
}
