package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.DirectionsClient;
import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.*;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Routing.Route;

import com.icl.fmfmc_backend.Integration.OsrDirectionsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//TODO: Incorporate double safety check for final destination charge level
//TODO: Handle situations whereby meeting the finalDestinationChargeLevel requires a stop very soon after eating.
//TODO: Significant issues with chargers opposite side of road being selected

@RequiredArgsConstructor
@Slf4j
@Service
public class RoutingService {

  private final DirectionsClientManager directionsClient;
  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
  private final ChargerService chargerService;
  private final GeometryService geometryService;
  private final PoiService poiService;

  public List<Charger> getChargersOnRoute(Route route) {
    logger.info("Fetching route from routing service");

    // Find all chargers in BufferedLineString

    ChargerQuery query =
        ChargerQuery.builder()
            .polygon(route.getBufferedLineString())
            //            .point(new GeometryFactory().createPoint(new
            // Coordinate(routeRequest.getStartLong(),routeRequest.getStartLat())))
            //            .radius(2000.0)
            .connectionTypeIds(route.getConnectionTypes())
            .minKwChargeSpeed(route.getMinKwChargeSpeed())
            .maxKwChargeSpeed(route.getMaxKwChargeSpeed())
            .minNoChargePoints(route.getMinNoChargePoints())
            .accessTypeIds(route.getAccessTypes())
            .build();

    List<Charger> chargersWithinPolygon = chargerService.getChargersByParams(query);

    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    return chargersWithinPolygon;

  }

  public LineString snapRouteToStops(Route route, List<Charger> suitableChargers) {

    logger.info("Snapping route to stops");

    List<Double[]> routeCoordinates = new ArrayList<>();

    Double[] startCoordinates = GeometryService.getPointAsDouble(route.getWorkingLineStringRoute().getStartPoint());
    routeCoordinates.add(startCoordinates);

    for (Charger charger : suitableChargers) {
      Double[] chargerCoordinates = GeometryService.getPointAsDouble(charger.getLocation());
      routeCoordinates.add(chargerCoordinates);
    }

    Double[] endCoordinates = GeometryService.getPointAsDouble(route.getWorkingLineStringRoute().getEndPoint());
    routeCoordinates.add(endCoordinates);

    // get OSR response
    DirectionsResponse directionsResponse =
            getDirections(routeCoordinates);

//    // get LineString from OSR response
//    LineString lineString =
//            geometryService.createLineString(
//                    osrDirectionsServiceGeoJSONResponse
//                            .getFeatures()
//                            .get(0)
//                            .getGeometry()
//                            .getCoordinates());

    // set segmentDetails
    route.setDurationsAndDistances(directionsResponse);
    route.setTotalDurationAndDistance(directionsResponse);

    return directionsResponse.getLineString();
  }


  /* Assisting Functions */

//  @Deprecated
//  public OSRDirectionsServiceGeoJSONResponse getOsrDirectionsServiceGeoJSONResponse(
//      List<Double[]> coordinates) {
//
//    OSRDirectionsServiceGeoJSONRequest osrDirectionsServiceGeoJSONRequest =
//        new OSRDirectionsServiceGeoJSONRequest(coordinates);
//
//    System.out.println(osrDirectionsServiceGeoJSONRequest);
//
//    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
//        osrClient.getDirectionsGeoJSON(osrDirectionsServiceGeoJSONRequest);
//    return osrDirectionsServiceGeoJSONResponse;
//  }

  public DirectionsResponse getDirections(
          List<Double[]> coordinates) {

    DirectionsRequest directionsRequest = new DirectionsRequest(coordinates);

    System.out.println(directionsRequest);

    DirectionsResponse directionsResponse =
            directionsClient.getDirections(directionsRequest);

    return directionsResponse;
  }

  // ROUTING RELATED FUNCTIONS

  public List<Charger> findSuitableChargers(Route route, List<Charger> potentialChargers) {
    Double maxTravelDistance =
        calculateMaxTravelDistance(
            route);
    logger.info("Max travel distance: " + maxTravelDistance);

    LinkedHashMap<Charger, Double> chargerDistanceMap = new LinkedHashMap<>();

    // map chargers to travel distance along route
    for (Charger charger : potentialChargers) {
      Double distanceToCharger =
              GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(
              route.getWorkingLineStringRoute(), charger.getLocation());
      chargerDistanceMap.put(charger, distanceToCharger);
    }

    // sort chargerDistanceMap map by distance
    chargerDistanceMap = chargerDistanceMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));

//  printChargerDistanceMap(chargerDistanceMap);

//    return potentialChargers.stream()
//        .filter(
//            charger ->
//                isChargerWithinTravelDistance(
//                    route.getLineStringRoute(), charger.getLocation(), maxTravelDistance))
//        .collect(Collectors.toList());

    List<Charger> suitableChargers = findChargersAtIntervals(chargerDistanceMap, route);
    logger.info("Suitable chargers: " + suitableChargers.size());
    return suitableChargers;

  }

  private Double calculateMaxTravelDistance(Route route) {
    Double useableBattery = route.getCurrentBattery() - route.getMinChargeLevel();
    if (useableBattery < route.getMinChargeLevel()) {
      return route.getCurrentBattery(); // if battery is below minChargeLevel return current battery
    } else {
      return useableBattery;
    }
  }

  @Deprecated
  private boolean isChargerWithinTravelDistance(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger = GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger <= maxDistance;
  }

  @Deprecated
  private Double getDistanceToChargerOnRoute(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger = GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger;
  }

  private List<Charger> findChargersAtIntervals(LinkedHashMap<Charger, Double> sortedChargers, Route route) {

    // TODO: optimise this to pick fastest chargers if reasonably close to interval, potentially make this a param.
    // TODO: increase accuracy of distance to charger by adding detour dist from route to charger.

    List<Charger> chargersAtIntervals = new ArrayList<>();
    Double nextTargetDistance = calculateMaxTravelDistance(route);
    Charger closestCharger = null;
    Double closestDistance = Double.MAX_VALUE;
    Double lastChargerDistance = 0.0; // to track the last chargers dist to calc current battery lvls

    logger.info("Total route length: " + route.getRouteLength());

    if (canCompleteRouteWithoutCharging(route) && !route.getStopForEating()) {
        route.reduceBattery(route.getRouteLength());
        logger.info("Charge level at end of route: " + route.getCurrentBattery() + " m");
        return Collections.emptyList();
    }

    // stop if charger is beyond the total route length
    for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
      Double chargerDistance = entry.getValue();

      if (route.getFoodAdjacentCharger() != null && entry.getKey().equals(route.getFoodAdjacentCharger())) {

        route.setFoodAdjacentChargerUsed(true);

        closestCharger = entry.getKey();
        closestDistance = chargerDistance;


        // calc range used up to this charger and update
        Double distanceTraveled = closestDistance - lastChargerDistance;
        lastChargerDistance = closestDistance;  // update the last charger's distance
        addChargerAndUpdateBattery(chargersAtIntervals, route.getFoodAdjacentCharger(), route, distanceTraveled);
        if (canCompleteRouteWithoutCharging(route)) {
          route.reduceBattery(route.getRouteLength() - closestDistance);
          logger.info("Charge level at end of route: " + route.getCurrentBattery() + " m");
          return chargersAtIntervals;
        }
        nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
        logger.info("Next target distance: " + nextTargetDistance + " m");

        // stop if the next interval is beyond the route end
        if (nextTargetDistance > route.getRouteLength()) {
          System.out.println("BREAKING: Adding food adjacent charger");
          break;
        }
        closestCharger = null; // reset for  next interval
        closestDistance = Double.MAX_VALUE;
        continue;
      }

      // exit loop if charger is beyond the total route length
      if (chargerDistance > route.getRouteLength()) {
        break;
      }

      if (chargerDistance <= nextTargetDistance) {
        // find closest charger to the target dist without exceeding it
        if (closestCharger == null || Math.abs(nextTargetDistance - chargerDistance) < Math.abs(nextTargetDistance - closestDistance)) {
          closestCharger = entry.getKey();
          closestDistance = chargerDistance;
        }
      }
      else {
        // once charger distance exceeds the target, add the last closest charger and move to next interval
        if (closestCharger != null) {

          // calc range used up to this charger and update
          Double distanceTraveled = closestDistance - lastChargerDistance;
          lastChargerDistance = closestDistance;  // update the last charger's distance

          addChargerAndUpdateBattery(chargersAtIntervals, closestCharger, route, distanceTraveled);

          nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
          logger.info("Next target distance: " + nextTargetDistance + " m");
          // stop if the next interval is beyond the route end
          if (nextTargetDistance > route.getRouteLength()) {
            break;
          }
          closestCharger = null; // reset for  next interval
          closestDistance = Double.MAX_VALUE;
        }
      }
    }

    // Add foodAdjacentCharger if not already added
    if (!route.getFoodAdjacentChargerUsed() && route.getFoodAdjacentCharger() != null) {
      route.setFoodAdjacentChargerUsed(true);
      closestCharger = route.getFoodAdjacentCharger();
      closestDistance = sortedChargers.get(route.getFoodAdjacentCharger());
      Double distanceTraveled = closestDistance - lastChargerDistance;
      lastChargerDistance = closestDistance;  // update the last charger's distance

      addChargerAndUpdateBattery(chargersAtIntervals, route.getFoodAdjacentCharger(), route, distanceTraveled);

      nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
      logger.info("Next target distance: " + nextTargetDistance + " m");
    }

    // add the last found closest charger if any and not already added
    if (closestCharger != null && !chargersAtIntervals.contains(closestCharger)) {
      logger.info("Adding charger in outer loop");
      Double finalSegmentDistance = closestDistance - lastChargerDistance;

      addChargerAndUpdateBattery(chargersAtIntervals, closestCharger, route, finalSegmentDistance);

    }

    // final update to battery level at route end
    Double currentBatteryAtFinalDestination = route.getCurrentBattery() - (route.getRouteLength() - closestDistance);

    if (currentBatteryAtFinalDestination < route.getFinalDestinationChargeLevel()) {
      if (!canEnsureFinalChargeLevel(chargersAtIntervals, route, sortedChargers)) {
          logger.info("Could not ensure final charge level ({})", route.getFinalDestinationChargeLevel());
          route.reduceBattery(route.getRouteLength() - closestDistance);
      }
    }
    else {
      route.reduceBattery(route.getRouteLength() - closestDistance);
    }

    logger.info("Distances for chargers added to chargersAtIntervals:");
    for (Charger charger : chargersAtIntervals) {
      Double distance = sortedChargers.get(charger);
      logger.info("Charger ID: " + charger.getId() + ", Distance: " + distance + " m");
    }

    logger.info("Charge level at end of route: " + route.getCurrentBattery() + " m");

//    printChargerDistanceMap(sortedChargers);

    return chargersAtIntervals;
  }

  private boolean canCompleteRouteWithoutCharging(Route route) {
    if (route.getRouteLength() <= route.getCurrentBattery() - route.getFinalDestinationChargeLevel()) {
      logger.info("Battery level is sufficient to complete the route without charging");
      return true;
    }
    return false;
  }

  private void addChargerAndUpdateBattery(List<Charger> chargersAtInterval,
                                          Charger selectedCharger,
                                          Route route,
                                          Double reducedBatteryBy) {
    chargersAtInterval.add(selectedCharger);
    route.reduceBattery(reducedBatteryBy);
    logger.info("Battery at: " + route.getCurrentBattery() + "m > " + route.getMinChargeLevel() + "m");
    route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(selectedCharger, route));
    // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
    logger.info("Recharging battery to: " + route.getCurrentBattery() + "m");

  }




  private boolean canEnsureFinalChargeLevel(List<Charger> chargersAtIntervals, Route route, LinkedHashMap<Charger, Double> sortedChargers) {
    if (chargersAtIntervals.isEmpty()) {
      return false; // no chargers added thus nothing to add
    }

    // get last charger and its distance
    Charger lastCharger = chargersAtIntervals.get(chargersAtIntervals.size() - 1);
    Double lastChargerDistance = sortedChargers.get(lastCharger);
    logger.info("Last charger distance: " + lastChargerDistance + " m");

    // how long left
    Double remainingDistance = route.getRouteLength() - lastChargerDistance;
    logger.info("Remaining distance: " + remainingDistance + " m");

    // calc the remaining battery after last charge and the rest of the route
    Double finalBatteryLevel = route.getChargeLevelAfterEachStop() - remainingDistance;
    logger.info("Final battery level: " + finalBatteryLevel + " m");
    logger.info("Final destination charge level: " + route.getFinalDestinationChargeLevel() + " m");

    // check if final battery level is below the required level
    // then check if there is a charger within the remaining distance
    if (finalBatteryLevel < route.getFinalDestinationChargeLevel()) {
      // need to add another charger to ensure final charge level
      for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
        Double chargerDistance = entry.getValue();
//        logger.info("Checking charger distance: " + chargerDistance + " m");
        // check if charger is within the remaining distance i.e. between last charger and final dest.
        if (chargerDistance > lastChargerDistance && chargerDistance <= route.getRouteLength()) {
          Charger candidateCharger = entry.getKey();
          Double distanceToCandidateCharger = chargerDistance - lastChargerDistance;
          Double batteryAfterReachingCandidate = route.getChargeLevelAfterEachStop() - distanceToCandidateCharger;

//          logger.info("Distance to candidate charger: " + distanceToCandidateCharger + " m");
//          logger.info("Battery after reaching candidate charger: " + batteryAfterReachingCandidate + " m");

//          logger.info("2nd comp: " + (route.getRouteLength() - chargerDistance) + " <= " + (route.getChargeLevelAfterEachStop() - route.getFinalDestinationChargeLevel()) + " ?");

          // CAN get to candidate charger with enough battery
          // AND remaining distance after candidate charger is LESS THAN the difference
          // between charge level after each stop and final destination charge level
          if (batteryAfterReachingCandidate >= route.getMinChargeLevel()
                  && route.getRouteLength() - chargerDistance // remaining distance after candidate charger
                  <= route.getChargeLevelAfterEachStop() - route.getFinalDestinationChargeLevel()) { // less than

            addChargerAndUpdateBattery(chargersAtIntervals, candidateCharger, route, distanceToCandidateCharger);
            logger.info("Added additional charger to ensure final charge level: Charger ID " + candidateCharger.getId());
            route.reduceBattery(route.getRouteLength() - chargerDistance);
            return true;
//            break;
          }
        }
      }
    }
    // fallback if final charge level is not met
    return ensureFinalChargeLevelFallback(chargersAtIntervals, route, sortedChargers, lastChargerDistance);
  }

  private Boolean ensureFinalChargeLevelFallback(List<Charger> chargersAtIntervals, Route route, LinkedHashMap<Charger, Double> sortedChargers, Double lastChargerDistance) {
    Charger lastSortedCharger = new ArrayList<>(sortedChargers.keySet()).get(sortedChargers.size() - 1);
    Double lastSortedDistance = sortedChargers.get(lastSortedCharger);

    if (!chargersAtIntervals.contains(lastSortedCharger)
            && lastSortedDistance > lastChargerDistance
            && lastSortedDistance <= route.getRouteLength()) {
      Double distanceToLastSortedCharger = lastSortedDistance - lastChargerDistance;
      addChargerAndUpdateBattery(chargersAtIntervals, lastSortedCharger, route, distanceToLastSortedCharger);
      logger.info("Added additional charger to partially meet final charge level: Charger ID " + lastSortedCharger.getId());
      route.reduceBattery(route.getRouteLength() - lastSortedDistance);
      return true;
    }
    return false;
  }


  public void printChargerDistanceMap(LinkedHashMap<Charger, Double> map) {
    for (Map.Entry<Charger, Double> entry : map.entrySet()) {
      Charger charger = entry.getKey();
      Double distance = entry.getValue();
      System.out.println("Charger ID: " + charger.getId() + ", Distance: " + distance + " m");
    }
  }

  @Deprecated
  private static String getPolylineAsString(
          OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse) {
    List<GeoCoordinates> routeCoordinates =
            osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getGeometry().getCoordinates();
    String polyline = PolylineUtility.encodeGeoCoordinatesToPolyline(routeCoordinates);
    System.out.println("Encoded Polyline: " + polyline.substring(0, 100) + "...");
    return polyline;
  }

}
