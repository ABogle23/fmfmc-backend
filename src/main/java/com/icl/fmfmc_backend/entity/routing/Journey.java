package com.icl.fmfmc_backend.entity.routing;

import com.icl.fmfmc_backend.geometry_service.GeometryService;
import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.dto.directions.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.*;
import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Journey {

  private static final GeometryService geometryService = new GeometryService();

  private static final double CHARGE_EFFICIENCY = 0.9; // used to calc charging time
  private static final double CHARGE_OVERHEAD =
      300; // time (s) to park, find & connect to charger etc

  private LineString originalLineStringRoute;
  private LineString workingLineStringRoute;
  private LineString finalSnappedToStopsRoute;
  private Polygon bufferedLineString;
  private String eatingOptionSearch;
  private List<Polygon> eatingSearchCircles = new ArrayList<>();
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
    private List<Double> chargeSpeedsKw = new ArrayList<>();
    private List<LocalTime> departTimes = new ArrayList<>();
    private List<LocalTime> arrivalTimes = new ArrayList<>();

    public void addSegment(Double duration, Double distance) {
      segmentDurations.add(duration);
      segmentDistances.add(distance);
    }

    private void clearSegmentDurationAndDistance() {
      segmentDurations  = new ArrayList<>();
      segmentDistances = new ArrayList<>();
    }

    public void addStopDuration(Double duration) {
      stopDurations.add(duration);
    }

    public void addChargeTime(Double arrivalCharge, Double departingCharge) {
      arrivalCharges.add(arrivalCharge);
      departingCharges.add(departingCharge);
    }

    public void addChargeSpeed(Double chargeSpeed) {
      chargeSpeedsKw.add(chargeSpeed);
    }

    public void addDepartTime(LocalTime departTime) {
      departTimes.add(departTime);
    }

    public void addArrivalTime(LocalTime arrivalTime) {
      arrivalTimes.add(arrivalTime);
    }

    public void clearTimes() {
      departTimes.clear();
      arrivalTimes.clear();
    }

    public void clearChargeSpeeds() {
      chargeSpeedsKw.clear();
    }

    public void clearStopDurations() {
      stopDurations.clear();
    }

    public void clearArrivalAndDepartureCharges() {
      arrivalCharges.clear();
      departingCharges.clear();
    }

    public void setStartAndEndBatteryLevel(Double startBatteryPct, Double endBatteryPct) {
      departingCharges.add(0, startBatteryPct);
      arrivalCharges.add(endBatteryPct);
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

  private LocalTime departTime;
  private LocalTime stoppingTime;

  private DeviationScope chargerSearchDeviation;
  private DeviationScope eatingOptionSearchDeviation;

  // TODO: consider weather conditions in range calculation

  // Standard Constructor
  private Journey(JourneyRequest journeyRequest) {

    this.startCoordinates = new Double[] {journeyRequest.getStartLong(), journeyRequest.getStartLat()};
    this.endCoordinates = new Double[] {journeyRequest.getEndLong(), journeyRequest.getEndLat()};

    this.StartBattery = journeyRequest.getStartingBattery();
    this.currentBattery = journeyRequest.getStartingBattery() * journeyRequest.getEvRange();
    this.evRange = journeyRequest.getEvRange();
    this.minChargeLevelPct = journeyRequest.getMinChargeLevel();
    this.minChargeLevel = journeyRequest.getMinChargeLevel() * journeyRequest.getEvRange();
    this.chargeLevelAfterEachStopPct = journeyRequest.getChargeLevelAfterEachStop();
    this.chargeLevelAfterEachStop =
        journeyRequest.getChargeLevelAfterEachStop() * journeyRequest.getEvRange();
    this.finalDestinationChargeLevelPct = journeyRequest.getFinalDestinationChargeLevel();
    this.finalDestinationChargeLevel =
        journeyRequest.getFinalDestinationChargeLevel() * journeyRequest.getEvRange();

    if (journeyRequest.getBatteryCapacity() != null) {
      this.batteryCapacity = journeyRequest.getBatteryCapacity();
    } else {
      this.batteryCapacity = (journeyRequest.getEvRange() * 200) / 1000000; // guesstimate
    }
    System.out.println("Battery capacity: " + this.batteryCapacity);

    this.accessTypes = journeyRequest.getAccessTypes();
    this.connectionTypes = journeyRequest.getConnectionTypes();
    this.minKwChargeSpeed = journeyRequest.getMinKwChargeSpeed();
    this.maxKwChargeSpeed = journeyRequest.getMaxKwChargeSpeed();
    this.minNoChargePoints = journeyRequest.getMinNoChargePoints();

    this.stopForEating = journeyRequest.getStopForEating();
    this.eatingOptions = journeyRequest.getEatingOptions();
    this.minPrice = journeyRequest.getMinPrice();
    this.maxPrice = journeyRequest.getMaxPrice();
    this.maxWalkingDistance = journeyRequest.getMaxWalkingDistance();
    this.includeAlternativeEatingOptions = journeyRequest.getIncludeAlternativeEatingOptions();
    this.stoppingRange = journeyRequest.getStoppingRange();

    this.departTime = journeyRequest.getDepartTime();
    this.stoppingTime = journeyRequest.getBreakDuration();

    this.chargerSearchDeviation = journeyRequest.getChargerSearchDeviation();
    this.eatingOptionSearchDeviation = journeyRequest.getEatingOptionSearchDeviation();
  }

  // Secondary Constructor using DirectionsResponse and RouteRequest

  public Journey(DirectionsResponse directionsResponse, JourneyRequest journeyRequest) {
    this(journeyRequest);
    this.originalLineStringRoute = directionsResponse.getLineString();
    this.workingLineStringRoute = originalLineStringRoute;
    this.bufferedLineString =
        GeometryService.bufferLineString(workingLineStringRoute, 0.009); // 1km
    this.routeLength = GeometryService.calculateLineStringLength(workingLineStringRoute);
    this.routeDuration = directionsResponse.getTotalDuration();
  }

  // Secondary Constructor coupled to OSRDirectionsServiceGeoJSONResponse

  @Deprecated
  public Journey(OSRDirectionsServiceGeoJSONResponse routeResponse, JourneyRequest journeyRequest) {
    this(journeyRequest);
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

  public void setTimes() {
    // updates route object routeLength and routeDuration to include detours to chargers and charge

    segmentDetails.addDepartTime(departTime);

    LocalTime workingTime = departTime;

    System.out.println("Working time: " + workingTime);
    for (int i = 0; i < segmentDetails.segmentDurations.size(); i++) {
      System.out.println("Working time: " + workingTime);

      workingTime = workingTime.plusSeconds(segmentDetails.segmentDurations.get(i).intValue());
      segmentDetails.addArrivalTime(workingTime);

      if (segmentDetails.stopDurations.size() > i) {

        if (foodAdjacentCharger != null
            && chargersOnRoute.get(i).equals(foodAdjacentCharger)
            && stoppingTime.toSecondOfDay() > segmentDetails.stopDurations.get(i).intValue()) {
          workingTime = workingTime.plusSeconds(stoppingTime.toSecondOfDay());
          segmentDetails.addDepartTime(workingTime);
        } else {
          workingTime = workingTime.plusSeconds(segmentDetails.stopDurations.get(i).intValue());
          segmentDetails.addDepartTime(workingTime);
        }
      }
    }
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
      case earliest, early -> this.stoppingRange = StoppingRange.extendedEarly;
      case middle -> this.stoppingRange = StoppingRange.extendedMiddle;
      case later, latest -> this.stoppingRange = StoppingRange.extendedLater;
      case extendedEarly, extendedLater, extendedMiddle -> {
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
//      case extreme -> {
//        return;
//      }
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
    if (minKwChargeSpeed != null && minKwChargeSpeed > 10) {
      minKwChargeSpeed = 10;
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

  public void resetSegmentDetails() {
    segmentDetails.clearArrivalAndDepartureCharges();
    segmentDetails.clearChargeSpeeds();
    segmentDetails.clearStopDurations();
    segmentDetails.clearTimes();
  }

  public void setStartAndEndBatteryLevels() {
    Double finalChargeLevel = this.currentBattery / evRange;
    segmentDetails.setStartAndEndBatteryLevel(StartBattery, finalChargeLevel);
  }
}
