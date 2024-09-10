package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.controller.JourneyController;
import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.charger.Connection;
import com.icl.fmfmc_backend.entity.routing.Journey;

import com.icl.fmfmc_backend.exception.integration.DirectionsClientException;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.exception.service.NoChargerWithinRangeException;
import com.icl.fmfmc_backend.exception.service.NoChargersOnRouteFoundException;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import com.icl.fmfmc_backend.util.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.max;

// TODO: Incorporate double safety check for final destination charge level
// TODO: Handle situations whereby meeting the finalDestinationChargeLevel requires a stop very soon
// after eating.
// TODO: Significant issues with chargers opposite side of road being selected

@RequiredArgsConstructor
@Slf4j
@Service
public class RoutingService {

  private final DirectionsClientManager directionsClient;
  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
  private final ChargerService chargerService;

  /**
   * Fetches chargers on the given route.
   *
   * @param journey the route for which chargers are to be fetched
   * @return a list of chargers on the route
   * @throws NoChargersOnRouteFoundException if no chargers are found on the route
   */
  @LogExecutionTime(message = LogMessages.GETTING_CHARGERS_ON_ROUTE)
  public List<Charger> getChargersOnRoute(Journey journey) throws NoChargersOnRouteFoundException {
    logger.info("Fetching route from routing service");

    ChargerQuery query =
        ChargerQuery.builder()
            .polygon(journey.getBufferedLineString())
            //            .point(new GeometryFactory().createPoint(new
            // Coordinate(routeRequest.getStartLong(),routeRequest.getStartLat())))
            //            .radius(2000.0)
            .connectionTypeIds(journey.getConnectionTypes())
            .minKwChargeSpeed(journey.getMinKwChargeSpeed())
            .maxKwChargeSpeed(journey.getMaxKwChargeSpeed())
            .minNoChargePoints(journey.getMinNoChargePoints())
            .accessTypeIds(journey.getAccessTypes())
            .build();

    List<Charger> chargersWithinPolygon = chargerService.getChargersByParams(query);

    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    //    for (Charger charger : chargersWithinPolygon) {
    //      System.out.println("Charger ID: " + charger.getId() + ", Charger Location: " +
    // charger.getLocation());
    //    }

    System.out.println("Chargers on route: " + chargersWithinPolygon.size());
    for (Charger charger : chargersWithinPolygon) {
      System.out.println(
          charger.getLocation().getY() + "," + charger.getLocation().getX() + ",red,circle");
    }

    if (chargersWithinPolygon.isEmpty()) {
      logger.error("No chargers found within the buffered route");
      throw new NoChargersOnRouteFoundException("No chargers found within the buffered route");
    }

    return chargersWithinPolygon;
  }

  /**
   * Snaps the given route to the stops provided.
   *
   * @param journey the route to be snapped
   * @param suitableChargers the list of suitable chargers
   * @return the snapped route as a LineString
   * @throws JourneyNotFoundException if no valid journey could be found
   */
  public LineString snapRouteToStops(Journey journey, List<Charger> suitableChargers)
      throws JourneyNotFoundException {

    logger.info("Snapping route to stops");

    List<Double[]> routeCoordinates = new ArrayList<>();

    Double[] startCoordinates =
        GeometryService.getPointAsDouble(journey.getWorkingLineStringRoute().getStartPoint());
    routeCoordinates.add(startCoordinates);

    for (Charger charger : suitableChargers) {
      Double[] chargerCoordinates = GeometryService.getPointAsDouble(charger.getLocation());
      routeCoordinates.add(chargerCoordinates);
    }

    Double[] endCoordinates =
        GeometryService.getPointAsDouble(journey.getWorkingLineStringRoute().getEndPoint());
    routeCoordinates.add(endCoordinates);

    // get OSR response
    DirectionsResponse directionsResponse = getDirections(routeCoordinates);

    //    // get LineString from OSR response
    //    LineString lineString =
    //            geometryService.createLineString(
    //                    osrDirectionsServiceGeoJSONResponse
    //                            .getFeatures()
    //                            .get(0)
    //                            .getGeometry()
    //                            .getCoordinates());

    // set segmentDetails
    journey.setDurationsAndDistances(directionsResponse);
    journey.setTotalDurationAndDistance(directionsResponse);

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

  /**
   * Fetches directions for the given coordinates.
   *
   * @param coordinates the list of coordinates for which directions are to be fetched
   * @return the directions response
   * @throws JourneyNotFoundException if no valid journey could be found
   */
  public DirectionsResponse getDirections(List<Double[]> coordinates)
      throws JourneyNotFoundException {

    //    directionsClient.setClient("mapbox");

    DirectionsRequest directionsRequest = new DirectionsRequest(coordinates);

    System.out.println(directionsRequest);

    DirectionsResponse directionsResponse = null;
    try {
      directionsResponse = directionsClient.getDirections(directionsRequest);
    } catch (DirectionsClientException e) {
      throw new JourneyNotFoundException("No valid journey could be found.");
    }

    //    System.out.println(directionsResponse);

    return directionsResponse;
  }

  // ROUTING RELATED FUNCTIONS

  /**
   * Finds suitable chargers for the given route and potential chargers.
   *
   * @param journey the route for which suitable chargers are to be found
   * @param potentialChargers the list of potential chargers
   * @return a list of suitable chargers
   * @throws NoChargerWithinRangeException if no chargers are found within range
   */
  @LogExecutionTime(message = LogMessages.FINDING_SUITABLE_CHARGERS)
  public List<Charger> findSuitableChargers(Journey journey, List<Charger> potentialChargers)
      throws NoChargerWithinRangeException {
    Double maxTravelDistance = calculateMaxTravelDistance(journey);
    logger.info("Max travel distance: " + maxTravelDistance);

    LinkedHashMap<Charger, Double> chargerDistanceMap = new LinkedHashMap<>();

    // map chargers to travel distance along route
    for (Charger charger : potentialChargers) {
      Double distanceToCharger =
          GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(
              journey.getWorkingLineStringRoute(), charger.getLocation());
      chargerDistanceMap.put(charger, distanceToCharger);
    }

    // sort chargerDistanceMap map by distance
    chargerDistanceMap =
        chargerDistanceMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));

    chargerDistanceMap = filterMatchingDistanceChargersByPower(chargerDistanceMap, journey);

    //  printChargerDistanceMap(chargerDistanceMap);

    List<Charger> suitableChargers = findChargersAtIntervals(chargerDistanceMap, journey);

    logger.info("Suitable chargers: " + suitableChargers.size());
    return suitableChargers;
  }

  private LinkedHashMap<Charger, Double> filterMatchingDistanceChargersByPower(
      LinkedHashMap<Charger, Double> chargerDistanceMap, Journey journey) {
    Map<Double, Charger> bestChargerAtEachDistance = new LinkedHashMap<>();

    chargerDistanceMap.forEach(
        (charger, distance) -> {
          // check if charger is foodAdjacentCharger
          Boolean isFoodAdjacentCharger =
              journey.getFoodAdjacentCharger() != null
                  && charger.equals(journey.getFoodAdjacentCharger());

          if (!bestChargerAtEachDistance.containsKey(distance) || isFoodAdjacentCharger) {
            bestChargerAtEachDistance.put(distance, charger);
          } else {
            Charger bestCharger = bestChargerAtEachDistance.get(distance);
            // Ensure food adjacent charger is not replaced by a higher power charger unless it's
            // also food adjacent
            if (!isFoodAdjacentCharger && getMaxPowerKW(charger) > getMaxPowerKW(bestCharger)) {
              System.out.println(
                  "Charger ID: "
                      + charger.getId()
                      + " has higher power "
                      + getMaxPowerKW(charger)
                      + " than Charger ID: "
                      + bestCharger.getId()
                      + " power: "
                      + getMaxPowerKW(bestCharger)
                      + " at distance: "
                      + distance
                      + "m");
              bestChargerAtEachDistance.put(distance, charger);
            }
          }
        });

    // add foodAdjacentCharger to bestChargerAtEachDistance, prevents it being overridden by higher
    // powerKw
    if (journey.getFoodAdjacentCharger() != null) {
      //      Charger charger = route.getFoodAdjacentCharger();
      Double distance = null;
      for (Map.Entry<Charger, Double> entry : chargerDistanceMap.entrySet()) {
        if (entry.getKey().getId().equals(journey.getFoodAdjacentCharger().getId())) {
          distance = entry.getValue();
          //          charger = entry.getKey();
          break;
        }
      }
      bestChargerAtEachDistance.put(distance, journey.getFoodAdjacentCharger());
      //      System.out.println("food adjacent charger: " + charger);
      //      System.out.println("food adjacent charger: " + route.getFoodAdjacentCharger());
    }

    // convert best chargers back to hashmap by dist
    LinkedHashMap<Charger, Double> filteredMap = new LinkedHashMap<>();
    bestChargerAtEachDistance.forEach((distance, charger) -> filteredMap.put(charger, distance));

    for (Map.Entry<Charger, Double> entry : filteredMap.entrySet()) {
      System.out.println(
          "Charger ID: " + entry.getKey().getId() + " at distance: " + entry.getValue() + "m");
    }

    return filteredMap;
  }

  private Long getMaxPowerKW(Charger charger) {
    return charger.getConnections().stream()
        .filter(conn -> conn.getPowerKW() != null)
        .map(Connection::getPowerKW)
        .max(Double::compare)
        .orElse(0L);
  }

  private Double calculateMaxTravelDistance(Journey journey) {
    Double useableBattery = journey.getCurrentBattery() - journey.getMinChargeLevel();
    // to be deleted
    //    if (useableBattery < route.getMinChargeLevel()) {
    //      return 0.0; // if battery is below minChargeLevel return current battery
    //    } else {
    //      return useableBattery;
    //    }
    return Math.max(useableBattery, 0.0);
  }

  @Deprecated
  private boolean isChargerWithinTravelDistance(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger =
        GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger <= maxDistance;
  }

  @Deprecated
  private Double getDistanceToChargerOnRoute(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger =
        GeometryService.calculateDistanceAlongRouteLineStringToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger;
  }

  private List<Charger> findChargersAtIntervals(
      LinkedHashMap<Charger, Double> sortedChargers, Journey journey)
      throws NoChargerWithinRangeException {

    // TODO: optimise this to pick fastest chargers if reasonably close to interval, potentially
    // make this a param.
    // TODO: increase accuracy of distance to charger by adding detour dist from route to charger.

    List<Charger> chargersAtIntervals = new ArrayList<>();
    Double nextTargetDistance = calculateMaxTravelDistance(journey);
    Charger closestCharger = null;
    Double closestDistance = Double.MAX_VALUE;
    Double lastChargerDistance =
        0.0; // to track the last chargers dist to calc current battery lvls

    logger.info("Total route length: " + journey.getRouteLength());

    if (canCompleteRouteWithoutCharging(journey) && !journey.getStopForEating()) {
      journey.reduceBattery(journey.getRouteLength());
      logger.info("Charge level at end of route: " + journey.getCurrentBattery() + " m");
      return Collections.emptyList();
    }

    // stop if charger is beyond the total route length
    for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
      Double chargerDistance = entry.getValue();

      logger.info(
          "1 Checking charger distance: "
              + chargerDistance
              + " m, Closest Charger: "
              + closestCharger); // REMOVE

      if (journey.getFoodAdjacentCharger() != null
          && entry.getKey().equals(journey.getFoodAdjacentCharger())) {

        // TODO: ADD Range Check
        System.out.println(
            "Charger distance: "
                + chargerDistance
                + "m"
                + " Last charger distance: "
                + lastChargerDistance
                + "m "
                + " Closest Distance: "
                + closestDistance
                + "m "
                + "Max travel distance: "
                + calculateMaxTravelDistance(journey)
                + "m"); // REMOVE
        if (((chargerDistance - lastChargerDistance) > calculateMaxTravelDistance(journey))) {
          if (((chargerDistance - closestDistance) > calculateMaxTravelDistance(journey))
              || closestCharger == null) {
            logger.error(
                "Could not find any chargers within range of foodAdjacentCharger based on current battery level and route.");
            throw new NoChargerWithinRangeException(
                "Unable to find any chargers within range based on current battery level and route.");
          } else if (closestCharger != null) {
            Double distanceTraveled = closestDistance - lastChargerDistance;
            lastChargerDistance = closestDistance;
            addChargerAndUpdateBattery(
                chargersAtIntervals, closestCharger, journey, distanceTraveled);
          }
        }

        journey.setFoodAdjacentChargerUsed(true);

        closestCharger = entry.getKey();
        closestDistance = chargerDistance;

        // calc range used up to this charger and update
        Double distanceTraveled = closestDistance - lastChargerDistance;
        lastChargerDistance = closestDistance; // update the last charger's distance
        addChargerAndUpdateBattery(
            chargersAtIntervals, journey.getFoodAdjacentCharger(), journey, distanceTraveled);
        if (canCompleteRouteWithoutCharging(journey)) {
          journey.reduceBattery(journey.getRouteLength() - closestDistance);
          logger.info("Charge level at end of route: " + journey.getCurrentBattery() + " m");
          return chargersAtIntervals;
        }

        nextTargetDistance =
            closestDistance + calculateMaxTravelDistance(journey); // calc next interval distance
        logger.info("Next target distance: " + nextTargetDistance + " m");

        // stop if the next interval is beyond the route end
        if (nextTargetDistance > journey.getRouteLength()) {
          System.out.println("BREAKING: Adding food adjacent charger");
          break;
        }
        closestCharger = null; // reset for  next interval
        closestDistance = Double.MAX_VALUE;
        continue;
      }

      // exit loop if charger is beyond the total route length
      // possibly redundant due to changes in charger selection
      if (chargerDistance > journey.getRouteLength()) {
        break;
      }

      if (chargerDistance <= nextTargetDistance) {
        // find closest charger to the target dist without exceeding it
        if (closestCharger == null
            || Math.abs(nextTargetDistance - chargerDistance)
                < Math.abs(nextTargetDistance - closestDistance)) {
          closestCharger = entry.getKey();
          closestDistance = chargerDistance;
        }
      } else {
        // once charger distance exceeds the target, add the last closest charger and move to next
        // interval
        if (closestCharger != null) {

          // calc range used up to this charger and update
          Double distanceTraveled = closestDistance - lastChargerDistance;
          lastChargerDistance = closestDistance; // update the last charger's distance

          addChargerAndUpdateBattery(chargersAtIntervals, closestCharger, journey, distanceTraveled);

          nextTargetDistance =
              closestDistance + calculateMaxTravelDistance(journey); // calc next interval distance
          logger.info("Next target distance: " + nextTargetDistance + " m");
          // stop if the next interval is beyond the route end
          if (nextTargetDistance > journey.getRouteLength()) {
            break;
          }
          closestCharger = null; // reset for  next interval
          closestDistance = Double.MAX_VALUE;
        } else {
          logger.error(
              "Could not find any chargers within range based on current battery level and route.");
          throw new NoChargerWithinRangeException(
              "Unable to find any chargers within range based on current battery level and route.");
        }
      }

      // TO BE DELETED for testing
      logger.info(
          "2 Checking charger distance: "
              + chargerDistance
              + " m, Closest Charger: "
              + closestCharger); // REMOVE
      if (entry.getValue().equals(max(sortedChargers.values()))) {
        logger.info("End of sortedChargers loop");
      }

      // if the dist between the last added charger and the current charger exceeds
      // maxTravelDistance, throw an exception
      System.out.println(
          "Charger distance: "
              + chargerDistance
              + "m"
              + " Last charger distance: "
              + lastChargerDistance
              + "m "
              + " Closest Distance: "
              + closestDistance
              + "m "
              + "Max travel distance: "
              + calculateMaxTravelDistance(journey)
              + "m"); // REMOVE
      if ((chargerDistance - lastChargerDistance) > calculateMaxTravelDistance(journey)) {
        throw new NoChargerWithinRangeException(
            "Unable to find any chargers within range based on current battery level and route.");
      }
    }

    // Add foodAdjacentCharger if not already added
    if (!journey.getFoodAdjacentChargerUsed() && journey.getFoodAdjacentCharger() != null) {
      journey.setFoodAdjacentChargerUsed(true);
      closestCharger = journey.getFoodAdjacentCharger();
      closestDistance = sortedChargers.get(journey.getFoodAdjacentCharger());
      Double distanceTraveled = closestDistance - lastChargerDistance;
      lastChargerDistance = closestDistance; // update the last charger's distance

      addChargerAndUpdateBattery(
          chargersAtIntervals, journey.getFoodAdjacentCharger(), journey, distanceTraveled);

      nextTargetDistance =
          closestDistance + calculateMaxTravelDistance(journey); // calc next interval distance
      logger.info("Next target distance: " + nextTargetDistance + " m");
    }

    // CAN DELETE add the last found closest charger if any and not already added
    if (closestCharger != null && !chargersAtIntervals.contains(closestCharger)) {
      logger.info("Adding charger in outer loop");
      Double finalSegmentDistance = closestDistance - lastChargerDistance;
      lastChargerDistance = closestDistance; // update the last charger's distance
      addChargerAndUpdateBattery(chargersAtIntervals, closestCharger, journey, finalSegmentDistance);
    }
    //    else if (closestCharger == null && (lastChargerDistance +
    // calculateMaxTravelDistance(route) < route.getRouteLength())) {
    //      logger.info("Last charger distance: " + lastChargerDistance + "m");
    //      logger.info("Max travel distance: " + calculateMaxTravelDistance(route) + "m");
    //      logger.error("Could not find any chargers within range based on current battery level
    // and route.");
    //      throw new NoChargerWithinRangeException("Unable to find any chargers within range based
    // on current battery level and route.");
    //
    //    }

    // final update to battery level at route end
    //    Double currentBatteryAtFinalDestination = route.getCurrentBattery() -
    // (route.getRouteLength() - closestDistance);
    Double currentBatteryAtFinalDestination =
        journey.getCurrentBattery() - (journey.getRouteLength() - lastChargerDistance);
    logger.info("Current battery before update: " + journey.getCurrentBattery() + " m"); // DELETE
    logger.info("Closest distance: " + closestDistance + " m"); // DELETE
    logger.info("Last charger distance: " + lastChargerDistance + " m"); // DELETE
    logger.info(
        "Current battery estimate at final destination: "
            + currentBatteryAtFinalDestination
            + " m"); // DELETE

    if (currentBatteryAtFinalDestination < journey.getFinalDestinationChargeLevel()) {
      if (!canEnsureFinalChargeLevel(chargersAtIntervals, journey, sortedChargers)) {
        // this is now redundant
        logger.info(
            "Could not ensure final charge level ({})", journey.getFinalDestinationChargeLevel());
        journey.reduceBattery(journey.getRouteLength() - closestDistance);
        logger.error(
            "Could not find any chargers within range based on current battery level and route.");
        throw new NoChargerWithinRangeException(
            "Unable to find any chargers within range based on current battery level and route.");
      }
    } else {
      journey.reduceBattery(journey.getRouteLength() - closestDistance);
    }

    logger.info("Distances for chargers added to chargersAtIntervals:");
    for (Charger charger : chargersAtIntervals) {
      Double distance = sortedChargers.get(charger);
      logger.info("Charger ID: " + charger.getId() + ", Distance: " + distance + " m");
    }

    logger.info("Charge level at end of route: " + journey.getCurrentBattery() + " m");

    printChargerDistanceMap(sortedChargers);

    return chargersAtIntervals;
  }

  private boolean canCompleteRouteWithoutCharging(Journey journey) {
    if (journey.getRouteLength()
        <= journey.getCurrentBattery() - journey.getFinalDestinationChargeLevel()) {
      logger.info("Battery level is sufficient to complete the route without charging");
      return true;
    }
    return false;
  }

  private void addChargerAndUpdateBattery(
      List<Charger> chargersAtInterval,
      Charger selectedCharger,
      Journey journey,
      Double reducedBatteryBy) {
    chargersAtInterval.add(selectedCharger);
    journey.reduceBattery(reducedBatteryBy);
    logger.info(
        "Battery at: " + journey.getCurrentBattery() + "m > " + journey.getMinChargeLevel() + "m");
    journey.rechargeBattery(
        chargerService.getHighestPowerConnectionByTypeInCharger(selectedCharger, journey));
    // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
    logger.info("Recharging battery to: " + journey.getCurrentBattery() + "m");
  }

  private boolean canEnsureFinalChargeLevel(
      List<Charger> chargersAtIntervals,
      Journey journey,
      LinkedHashMap<Charger, Double> sortedChargers) {
    if (chargersAtIntervals.isEmpty()) {
      return false; // no chargers added thus nothing to add
    }

    // get last charger and its distance
    Charger lastCharger = chargersAtIntervals.get(chargersAtIntervals.size() - 1);
    Double lastChargerDistance = sortedChargers.get(lastCharger);
    logger.info("Last charger distance: " + lastChargerDistance + " m");

    // how long left
    Double remainingDistance = journey.getRouteLength() - lastChargerDistance;
    logger.info("Remaining distance: " + remainingDistance + " m");

    // calc the remaining battery after last charge and the rest of the route
    Double finalBatteryLevel = journey.getChargeLevelAfterEachStop() - remainingDistance;
    logger.info("Final battery level: " + finalBatteryLevel + " m");
    logger.info("Final destination charge level: " + journey.getFinalDestinationChargeLevel() + " m");

    // check if final battery level is below the required level
    // then check if there is a charger within the remaining distance
    if (finalBatteryLevel < journey.getFinalDestinationChargeLevel()) {
      // need to add another charger to ensure final charge level
      for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
        Double chargerDistance = entry.getValue();
        //        logger.info("Checking charger distance: " + chargerDistance + " m");
        // check if charger is within the remaining distance i.e. between last charger and final
        // dest.
        if (chargerDistance > lastChargerDistance && chargerDistance <= journey.getRouteLength()) {
          Charger candidateCharger = entry.getKey();
          Double distanceToCandidateCharger = chargerDistance - lastChargerDistance;
          Double batteryAfterReachingCandidate =
              journey.getChargeLevelAfterEachStop() - distanceToCandidateCharger;

          //          logger.info("Distance to candidate charger: " + distanceToCandidateCharger + "
          // m");
          //          logger.info("Battery after reaching candidate charger: " +
          // batteryAfterReachingCandidate + " m");

          //          logger.info("2nd comp: " + (route.getRouteLength() - chargerDistance) + " <= "
          // + (route.getChargeLevelAfterEachStop() - route.getFinalDestinationChargeLevel()) + "
          // ?");

          // CAN get to candidate charger with enough battery
          // AND remaining distance after candidate charger is LESS THAN the difference
          // between charge level after each stop and final destination charge level
          if (batteryAfterReachingCandidate >= journey.getMinChargeLevel()
              && journey.getRouteLength()
                      - chargerDistance // remaining distance after candidate charger
                  <= journey.getChargeLevelAfterEachStop()
                      - journey.getFinalDestinationChargeLevel()) { // less than

            addChargerAndUpdateBattery(
                chargersAtIntervals, candidateCharger, journey, distanceToCandidateCharger);
            logger.info(
                "Added additional charger to ensure final charge level: Charger ID "
                    + candidateCharger.getId());
            journey.reduceBattery(journey.getRouteLength() - chargerDistance);
            return true;
            //            break;
          }
        }
      }
    }
    // fallback if final charge level is not met
    return ensureFinalChargeLevelFallback(
        chargersAtIntervals, journey, sortedChargers, lastChargerDistance);
  }

  private Boolean ensureFinalChargeLevelFallback(
      List<Charger> chargersAtIntervals,
      Journey journey,
      LinkedHashMap<Charger, Double> sortedChargers,
      Double lastChargerDistance) {
    Charger lastSortedCharger =
        new ArrayList<>(sortedChargers.keySet()).get(sortedChargers.size() - 1);
    Double lastSortedDistance = sortedChargers.get(lastSortedCharger);

    Double distanceToLastSortedCharger = lastSortedDistance - lastChargerDistance;
    if (!chargersAtIntervals.contains(lastSortedCharger)
        && lastSortedDistance > lastChargerDistance
        && lastSortedDistance <= journey.getRouteLength()
    //    && calculateMaxTravelDistance(route) >= distanceToLastSortedCharger
    ) { // added line to slop looping issue in findSuitableChargers
      logger.info("Distance to last sorted charger: " + distanceToLastSortedCharger + " m");
      addChargerAndUpdateBattery(
          chargersAtIntervals, lastSortedCharger, journey, distanceToLastSortedCharger);
      logger.info(
          "Added additional charger to partially meet final charge level: Charger ID "
              + lastSortedCharger.getId());
      journey.reduceBattery(journey.getRouteLength() - lastSortedDistance);
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

}
