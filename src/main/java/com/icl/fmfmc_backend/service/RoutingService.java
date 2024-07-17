package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONRequest;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.*;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;

import com.icl.fmfmc_backend.Integration.OSRClient;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;

import org.locationtech.jts.operation.distance.DistanceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

//TODO: Incorporate double safety check for final destination charge level
//TODO: Handle situations whereby meeting the finalDestinationChargeLevel requires a stop very soon after eating.
//TODO: Significant issues with chargers opposite side of road being selected

@RequiredArgsConstructor
@Slf4j
@Service
public class RoutingService {

  private final OSRClient osrClient;
  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;
  private final GeometryService geometryService;
  private final PoiService poiService;

  public RouteResult getRoute(RouteRequest routeRequest) {
    logger.info("Fetching initial route from routing service");

    // get initial route
    Double[] startCoordinates = {routeRequest.getStartLong(), routeRequest.getStartLat()};
    Double[] endCoordinates = {routeRequest.getEndLong(), routeRequest.getEndLat()};
    List<Double[]> startAndEndCoordinates = List.of(startCoordinates, endCoordinates);
    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
        getOsrDirectionsServiceGeoJSONResponse(startAndEndCoordinates);

    // Build Route Object
    Route route = new Route(osrDirectionsServiceGeoJSONResponse, routeRequest);


    /* -----Find ideal FoodEstablishment via PoiService----- */

//    Tuple2<List<FoodEstablishment>, Charger> poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(route, routeRequest);
//
//    route.setFoodEstablishments(poiServiceTestResults.getT1());
//    route.setFoodAdjacentCharger(poiServiceTestResults.getT2());
//    LineString routeSnappedToFoodAdjacentCharger = snapRouteToStops(route, List.of(route.getFoodAdjacentCharger()));
//    route.setWorkingLineStringRoute(routeSnappedToFoodAdjacentCharger);
//    Polygon bufferedLineString =
//            GeometryService.bufferLineString(routeSnappedToFoodAdjacentCharger, 0.009); // 500m is 0.0045
//    route.setBufferedLineString(bufferedLineString);


    /* -----Find all chargers in BufferedLineString----- */

    ChargerQuery query =
        ChargerQuery.builder()
            .polygon(route.getBufferedLineString())
            //            .point(new GeometryFactory().createPoint(new
            // Coordinate(routeRequest.getStartLong(),routeRequest.getStartLat())))
            //            .radius(2000.0)
            .connectionTypeIds(routeRequest.getConnectionTypes())
            .minKwChargeSpeed(routeRequest.getMinKwChargeSpeed())
            .maxKwChargeSpeed(routeRequest.getMaxKwChargeSpeed())
            .minNoChargePoints(routeRequest.getMinNoChargePoints())
            .accessTypeIds(routeRequest.getAccessTypes())
            .build();

    List<Charger> chargersWithinPolygon = chargerService.getChargersByParams(query);

    logger.info("Chargers within query: " + chargersWithinPolygon.size());


    /* -----Filter chargers to those reachable on route based on battery level----- */

    List<Charger> suitableChargers = findSuitableChargers(route, chargersWithinPolygon);
    logger.info("Suitable chargers: " + suitableChargers.size());
    route.setChargersOnRoute(suitableChargers);


    /* -----Snap route to optimal Chargers and FoodEstablishments----- */

    LineString routeSnappedToStops = snapRouteToStops(route, suitableChargers);
    route.setFinalSnappedToStopsRoute(routeSnappedToStops);


    /* -----Build Result----- */

    List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();
    RouteResult routeResult = new RouteResult(route, routeRequest);

    return routeResult;
  }

  private LineString snapRouteToStops(Route route, List<Charger> suitableChargers) {

    logger.info("Snapping route to stops");

    List<Double[]> routeCoordinates = new ArrayList<>();

    Double[] startCoordinates = getPointAsDouble(route.getWorkingLineStringRoute().getStartPoint());
    routeCoordinates.add(startCoordinates);

    for (Charger charger : suitableChargers) {
      Double[] chargerCoordinates = getPointAsDouble(charger.getLocation());
      routeCoordinates.add(chargerCoordinates);
    }

    Double[] endCoordinates = getPointAsDouble(route.getWorkingLineStringRoute().getEndPoint());
    routeCoordinates.add(endCoordinates);

    // get OSR response
    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
            getOsrDirectionsServiceGeoJSONResponse(routeCoordinates);

    // get LineString from OSR response
    LineString lineString =
            geometryService.createLineString(
                    osrDirectionsServiceGeoJSONResponse
                            .getFeatures()
                            .get(0)
                            .getGeometry()
                            .getCoordinates());

    // set segmentDetails
    route.setDurationsAndDistances(osrDirectionsServiceGeoJSONResponse);
    route.setTotalDurationAndDistance(osrDirectionsServiceGeoJSONResponse);

    return lineString;
  }

  private Double[] getPointAsDouble(Point point) {
    return new Double[]{point.getX(), point.getY()};
  }


  /* Assisting Functions */

  private static String getPolylineAsString(
      OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse) {
    List<GeoCoordinates> routeCoordinates =
        osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getGeometry().getCoordinates();
    String polyline = PolylineUtility.encodeGeoCoordinatesToPolyline(routeCoordinates);
    System.out.println("Encoded Polyline: " + polyline.substring(0, 100) + "...");
    return polyline;
  }

  private OSRDirectionsServiceGeoJSONResponse getOsrDirectionsServiceGeoJSONResponse(
      List<Double[]> coordinates) {
//    logger.info("Fetching initial route");

    OSRDirectionsServiceGeoJSONRequest osrDirectionsServiceGeoJSONRequest =
        new OSRDirectionsServiceGeoJSONRequest(coordinates);

    System.out.println(osrDirectionsServiceGeoJSONRequest);

    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
        osrClient.getDirectionsGeoJSON(osrDirectionsServiceGeoJSONRequest);
    return osrDirectionsServiceGeoJSONResponse;
  }

  // ROUTING RELATED FUNCTIONS

  private List<Charger> findSuitableChargers(Route route, List<Charger> potentialChargers) {
    Double maxTravelDistance =
        calculateMaxTravelDistance(
            route);
    logger.info("Max travel distance: " + maxTravelDistance);

    LinkedHashMap<Charger, Double> chargerDistanceMap = new LinkedHashMap<>();

    // map chargers to travel distance along route
    for (Charger charger : potentialChargers) {
      Double distanceToCharger =
          calculateDistanceAlongRouteToNearestPoint(
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

    return findChargersAtIntervals(chargerDistanceMap, route);


  }

  private Double calculateMaxTravelDistance(Route route) {
    Double useableBattery = route.getCurrentBattery() - route.getMinChargeLevel();
    if (useableBattery < route.getMinChargeLevel()) {
      return route.getCurrentBattery(); // if battery is below minChargeLevel return current battery
    } else {
      return useableBattery;
    }
  }

  private boolean isChargerWithinTravelDistance(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger = calculateDistanceAlongRouteToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger <= maxDistance;
  }

  private Double getDistanceToChargerOnRoute(
      LineString route, Point chargerLocation, Double maxDistance) {
    Double distanceToCharger = calculateDistanceAlongRouteToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger;
  }

  private Double calculateDistanceAlongRouteToNearestPoint(
      LineString route, Point chargerLocation) {
    Coordinate[] nearestCoordinatesOnRoute = DistanceOp.nearestPoints(route, chargerLocation);
    Point nearestPointOnRoute = new GeometryFactory().createPoint(nearestCoordinatesOnRoute[0]);
    nearestPointOnRoute =
        new GeometryFactory().createPoint(findClosestCoordinateOnLine(route, chargerLocation));
//    logger.info("==========Next Charger==========");
//    logger.info(
//        "Nearest point on route: " + nearestPointOnRoute + " to charger: " + chargerLocation);

    Double cumulativeDistance = 0.0;
    Point lastPoint = (Point) route.getStartPoint();

    for (int i = 0; i < route.getNumPoints(); i++) {
      Point currentPoint = route.getPointN(i);

      Double segmentDistance = GeometryService.calculateDistanceBetweenPoints(
              lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX()); // dist in meters
      //      if (i % 100 == 0) {
      //        logger.info("Cumulative distance - pt " + i + " : " + cumulativeDistance);
      //      }
      if (currentPoint.equalsExact(nearestPointOnRoute, 0.00001)
          || i == route.getNumPoints() - 1) {
        cumulativeDistance += segmentDistance;
        break;
      }

      cumulativeDistance += segmentDistance;
      lastPoint = currentPoint;
    }

//    logger.info("Cumulative distance: " + cumulativeDistance);
//    logger.info("lastPoint: " + lastPoint);
    return cumulativeDistance;
  }

  public Coordinate findClosestCoordinateOnLine(LineString lineString, Point chargerLocation) {

    Double minDistance = Double.MAX_VALUE;
    Coordinate closestCoordinate = null;

    for (Coordinate coordinate : lineString.getCoordinates()) {

      Double distance = GeometryService.calculateDistanceBetweenPoints(
              chargerLocation.getY(), chargerLocation.getX(), coordinate.y, coordinate.x); // dist in meters

      if (distance < minDistance) {
        minDistance = distance;
        closestCoordinate = coordinate;
      }
    }
//    logger.info("Closest coordinate found: " + closestCoordinate + " with distance: " + minDistance);

    return closestCoordinate;
  }





  private List<Charger> findChargersAtIntervals(LinkedHashMap<Charger, Double> sortedChargers, Route route) {

    // TODO: optimise this to pick fastest chargers if reasonably close to interval, potentially make this a param.
    // TODO: increase accuracy of distance to charger by adding detour dist from route to charger.
    // TODO: clean up code asap, break up into functions.

    List<Charger> chargersAtIntervals = new ArrayList<>();
    Double nextTargetDistance = calculateMaxTravelDistance(route);
    Charger closestCharger = null;
    Double closestDistance = Double.MAX_VALUE;
    Double lastChargerDistance = 0.0; // to track the last chargers dist to calc current battery lvls

    logger.info("Total route length: " + route.getRouteLength());

    if (route.getRouteLength() <= route.getCurrentBattery() - route.getFinalDestinationChargeLevel()) {
        logger.info("Battery level is sufficient to complete the route without charging");
        return Collections.emptyList();
    }

    // stop if charger is beyond the total route length
    for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
      Double chargerDistance = entry.getValue();

      if (route.getFoodAdjacentCharger() != null && entry.getKey().equals(route.getFoodAdjacentCharger())) {

        closestCharger = entry.getKey();
        closestDistance = chargerDistance;

        chargersAtIntervals.add(route.getFoodAdjacentCharger());
        route.setFoodAdjacentChargerUsed(true);

        // calc range used up to this charger and update
        Double distanceTraveled = closestDistance - lastChargerDistance;
        lastChargerDistance = closestDistance;  // update the last charger's distance
        route.setCurrentBattery(route.getCurrentBattery() - distanceTraveled);
        logger.info("Battery at: " + route.getCurrentBattery() + " m");
        route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(route.getFoodAdjacentCharger(), route));
        // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
        logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
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
          chargersAtIntervals.add(closestCharger);

          // calc range used up to this charger and update
          Double distanceTraveled = closestDistance - lastChargerDistance;
          lastChargerDistance = closestDistance;  // update the last charger's distance
          route.setCurrentBattery(route.getCurrentBattery() - distanceTraveled);
          logger.info("Battery at: " + route.getCurrentBattery() + " m");
          route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(closestCharger, route));
          // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
          logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
          nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
          logger.info("Next target distance: " + nextTargetDistance + " m");
          // stop if the next interval is beyond the route end
          if (nextTargetDistance > route.getRouteLength()) {

            if (!route.getFoodAdjacentChargerUsed() && route.getFoodAdjacentCharger() != null) {
              // TODO: Implement edge case
            }

            break;
          }
          closestCharger = null; // reset for  next interval
          closestDistance = Double.MAX_VALUE;
        }
      }
    }

    // Add foodAdjacentCharger if not already added
    if (!route.getFoodAdjacentChargerUsed() && route.getFoodAdjacentCharger() != null) {
      closestCharger = route.getFoodAdjacentCharger();
      closestDistance = sortedChargers.get(route.getFoodAdjacentCharger());
      System.out.println(sortedChargers.get(route.getFoodAdjacentCharger()));
      chargersAtIntervals.add(route.getFoodAdjacentCharger());
      route.setFoodAdjacentChargerUsed(true);
      Double distanceTraveled = closestDistance - lastChargerDistance;
      lastChargerDistance = closestDistance;  // update the last charger's distance
      route.setCurrentBattery(route.getCurrentBattery() - distanceTraveled);
      route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(closestCharger, route));
      // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
      logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
      nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
      logger.info("Next target distance: " + nextTargetDistance + " m");
    }

    // add the last found closest charger if any and not already added
    if (closestCharger != null && !chargersAtIntervals.contains(closestCharger)) {
      logger.info("Adding charger in outer loop");
      chargersAtIntervals.add(closestCharger);
      Double finalSegmentDistance = closestDistance - lastChargerDistance;
      route.setCurrentBattery(route.getCurrentBattery() - (finalSegmentDistance));
      logger.info("Battery at: " + route.getCurrentBattery() + " m");
      route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(closestCharger, route));
      logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
    }

    // final update to battery level at route end
    Double currentBatteryAtFinalDestination = route.getCurrentBattery() - (route.getRouteLength() - closestDistance);

    if (currentBatteryAtFinalDestination < route.getFinalDestinationChargeLevel()) {
      if (!canEnsureFinalChargeLevel(chargersAtIntervals, route, sortedChargers)) {
          logger.info("Could not ensure final charge level ({})", route.getFinalDestinationChargeLevel());
          route.setCurrentBattery(route.getCurrentBattery() - (route.getRouteLength() - closestDistance));
      }
    }
    else {
      route.setCurrentBattery(route.getCurrentBattery() - (route.getRouteLength() - closestDistance));
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

    // Calculate the remaining battery after last charge and the rest of the route
    Double finalBatteryLevel = route.getChargeLevelAfterEachStop() - remainingDistance;
    logger.info("Final battery level: " + finalBatteryLevel + " m");
    logger.info("Final destination charge level: " + route.getFinalDestinationChargeLevel() + " m");

    // check if final battery level is below the required level
    // then check if there is a charger within the remaining distance
    if (finalBatteryLevel < route.getFinalDestinationChargeLevel()) {
      // need to add another charger to ensure final charge level
      for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
        Double chargerDistance = entry.getValue();
        logger.info("Checking charger distance: " + chargerDistance + " m");
        // check if charger is within the remaining distance i.e. between last charger and final dest.
        if (chargerDistance > lastChargerDistance && chargerDistance <= route.getRouteLength()) {
          Charger candidateCharger = entry.getKey();
          Double distanceToCandidateCharger = chargerDistance - lastChargerDistance;
          Double batteryAfterReachingCandidate = route.getChargeLevelAfterEachStop() - distanceToCandidateCharger;

          logger.info("Distance to candidate charger: " + distanceToCandidateCharger + " m");
          logger.info("Battery after reaching candidate charger: " + batteryAfterReachingCandidate + " m");

          logger.info("2nd comp: " + (route.getRouteLength() - chargerDistance) + " <= " + (route.getChargeLevelAfterEachStop() - route.getFinalDestinationChargeLevel()) + " ?");

          // CAN get to candidate charger with enough battery
          // AND remaining distance after candidate charger is LESS THAN the difference
          // between charge level after each stop and final destination charge level
          if (batteryAfterReachingCandidate >= route.getMinChargeLevel()
                  && route.getRouteLength() - chargerDistance // remaining distance after candidate charger
                  <= route.getChargeLevelAfterEachStop() - route.getFinalDestinationChargeLevel()) { // less than
            chargersAtIntervals.add(candidateCharger);

            route.setCurrentBattery(route.getCurrentBattery() - distanceToCandidateCharger);
            logger.info("Battery at: " + route.getCurrentBattery() + " m");
            route.rechargeBattery(chargerService.getHighestPowerConnectionByTypeInCharger(candidateCharger, route));
            logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");

            logger.info("Added additional charger to ensure final charge level: Charger ID " + candidateCharger.getId());
            route.setCurrentBattery(route.getCurrentBattery() - (route.getRouteLength() - chargerDistance));
            return true;
//            break;
            // TODO: Add best attempt at finding a charger
          }
        }
      }
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
