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
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequest;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequestBuilder;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import com.icl.fmfmc_backend.Integration.OSRClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;

import org.locationtech.jts.algorithm.*;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

import org.locationtech.jts.operation.distance.DistanceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class RoutingService {

  private final OSRClient osrClient;
  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;
  private final GeometryService geometryService;

  public RouteResult getRoute(RouteRequest routeRequest) {
    logger.info("Fetching initial route from routing service");

    // get initial route
    Double[] startCoordinates = {routeRequest.getStartLong(), routeRequest.getStartLat()};
    Double[] endCoordinates = {routeRequest.getEndLong(), routeRequest.getEndLat()};
    List<Double[]> startAndEndCoordinates = List.of(startCoordinates, endCoordinates);
    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
        getOsrDirectionsServiceGeoJSONResponse(startAndEndCoordinates);

    // get LineString
    LineString lineString =
        geometryService.createLineString(
            osrDirectionsServiceGeoJSONResponse
                .getFeatures()
                .get(0)
                .getGeometry()
                .getCoordinates());

    // buffer LineString
    Polygon bufferedLineString =
        GeometryService.bufferLineString(lineString, 0.009); // 500m is 0.0045
    System.out.println("Original LineString: " + lineString.toText().substring(0, 100) + "...");
    System.out.println(
        "Buffered Polygon: " + bufferedLineString.toText().substring(0, 100) + "...");

    // Build Route Object with LineString and buffered LineString
//    Route route =
//        new Route(
//            lineString,
//            bufferedLineString,
//            osrDirectionsServiceGeoJSONResponse
//                .getFeatures()
//                .get(0)
//                .getProperties()
//                .getSummary()
//                .getDistance(),
//            osrDirectionsServiceGeoJSONResponse
//                .getFeatures()
//                .get(0)
//                .getProperties()
//                .getSummary()
//                .getDuration(),
//            routeRequest);
      Route route = new Route(osrDirectionsServiceGeoJSONResponse, routeRequest);


    // convert polyline & polygon to Strings
    String polyline = getPolylineAsString(osrDirectionsServiceGeoJSONResponse);
    String tmpPolygon = PolylineUtility.encodePolygon(bufferedLineString);

    // find chargers based on buffered LineString

    long startTime = System.currentTimeMillis();

    ChargerQuery query =
        ChargerQuery.builder()
            .polygon(bufferedLineString)
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

    long endTime = System.currentTimeMillis();
    Double duration = (endTime - startTime) / 1000.0;
    logger.info(String.format("Processing chargers SQL query, Processing time: %.2f s", duration));


    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    // filter chargers to those reachable from the route based on battery level
    List<Charger> suitableChargers = findSuitableChargers(route, chargersWithinPolygon);
    logger.info("Suitable chargers: " + suitableChargers.size());


    // Snap route to Chargers and FoodEstablishments
    LineString routeSnappedToStops = snapRouteToStops(route, suitableChargers);
    route.setRouteSnappedToStops(routeSnappedToStops);
    polyline = PolylineUtility.encodeLineString(routeSnappedToStops);


    // find FoodEstablishments based on buffered LineString

    Polygon foursquareBufferedLineString =
            GeometryService.bufferLineString(lineString, 0.018); // 500m is 0.0045
    String tmpPolygonFoursquareFormat = polygonStringToFoursquareFormat(foursquareBufferedLineString);
    logger.info("Fetching food establishments within polygon from Foursquare, Str Len: {}", tmpPolygonFoursquareFormat.length());
    if (tmpPolygonFoursquareFormat.length() > 100) {
      logger.info("tmpPolygonFoursquareFormat " + tmpPolygonFoursquareFormat.substring(0, 100) + "...");
    } else {
      logger.info("tmpPolygonFoursquareFormat " + tmpPolygonFoursquareFormat);
    }

    FoursquareRequest params =
        new FoursquareRequestBuilder()
            .setCategories(routeRequest.getEatingOptions())
            .setPolygon(tmpPolygonFoursquareFormat)
            .createFoursquareRequest();

//        List<FoodEstablishment> foodEstablishmentsWithinPolygon =
//            foodEstablishmentService.getFoodEstablishmentsByParam(params);
    List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();

    // build result
    RouteResult dummyRouteResult =
        getRouteResult(
            routeRequest,
                polyline,
                tmpPolygon,
                tmpPolygonFoursquareFormat,
                route.getRouteLength(),
                route.getRouteDuration(),
                route.getSegmentDetails(),
                suitableChargers,
                foodEstablishmentsWithinPolygon);

    return dummyRouteResult;
  }

  private LineString snapRouteToStops(Route route, List<Charger> suitableChargers) {

    logger.info("Snapping route to stops");

    List<Double[]> routeCoordinates = new ArrayList<>();

    Double[] startCoordinates = getPointAsDouble(route.getLineStringRoute().getStartPoint());
    routeCoordinates.add(startCoordinates);

    for (Charger charger : suitableChargers) {
      Double[] chargerCoordinates = getPointAsDouble(charger.getLocation());
      routeCoordinates.add(chargerCoordinates);
    }

    Double[] endCoordinates = getPointAsDouble(route.getLineStringRoute().getEndPoint());
    routeCoordinates.add(endCoordinates);


    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
            getOsrDirectionsServiceGeoJSONResponse(routeCoordinates);

    // get LineString
    LineString lineString =
            geometryService.createLineString(
                    osrDirectionsServiceGeoJSONResponse
                            .getFeatures()
                            .get(0)
                            .getGeometry()
                            .getCoordinates());

    // set segments
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

  private RouteResult getRouteResult(
      RouteRequest routeRequest,
      String polyline,
      String tmpPolygon,
      String tmpPolygonFoursquareFormat,
      Double totalDistance,
      Double totalDuration,
      Route.SegmentDetails segmentDetails,
      List<Charger> chargers,
      List<FoodEstablishment> foodEstablishments) {

    RouteResult dummyRouteResult =
        new RouteResult(
            polyline, tmpPolygon, tmpPolygonFoursquareFormat, totalDistance, totalDuration, segmentDetails, chargers, foodEstablishments, routeRequest);
    return dummyRouteResult;
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

  public static String polygonStringToFoursquareFormat(Polygon polygon) {

    int step = 75;
    Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
    StringBuilder formattedString = new StringBuilder();

    // iterate over coordinates skipping {step} coordinates each time
    for (int i = 0; i < coordinates.length; i += step) {
      // use CoordinateFormatter to trim lat long to 3dp
      formattedString
          .append(CoordinateFormatter.formatCoordinate(coordinates[i].y))
          .append(",")
          .append(CoordinateFormatter.formatCoordinate(coordinates[i].x));

      // append '~' separator except after last coordinate
      if (i + step < coordinates.length) {
        formattedString.append("~");
      }
    }

    // ensure the polygon is closed (first and last coordinates should be the same)
    if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
      formattedString
          .append("~")
          .append(CoordinateFormatter.formatCoordinate(coordinates[0].y))
          .append(",")
          .append(CoordinateFormatter.formatCoordinate(coordinates[0].x));
    }

    return formattedString.toString();
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
              route.getLineStringRoute(), charger.getLocation());
      chargerDistanceMap.put(charger, distanceToCharger);
    }

    // sort chargerDistanceMap map by distance
    chargerDistanceMap = chargerDistanceMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  printChargerDistanceMap(chargerDistanceMap);


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
    logger.info("==========Next Charger==========");
    logger.info(
        "Nearest point on route: " + nearestPointOnRoute + " to charger: " + chargerLocation);

    Double cumulativeDistance = 0.0;
    Point lastPoint = (Point) route.getStartPoint();

    for (int i = 1; i < route.getNumPoints(); i++) {
      Point currentPoint = route.getPointN(i);
      GeodesicData g =
          Geodesic.WGS84.Inverse(
              lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX());
      Double segmentDistance = g.s12; // distance in meters
      //      if (i % 100 == 0) {
      //        logger.info("Cumulative distance - pt " + i + " : " + cumulativeDistance);
      //      }
      if (currentPoint.equalsExact(nearestPointOnRoute, 0.000001)
          || i == route.getNumPoints() - 1) {
        cumulativeDistance += segmentDistance;
        break;
      }

      cumulativeDistance += segmentDistance;
      lastPoint = currentPoint;
    }

    logger.info("Cumulative distance: " + cumulativeDistance);
    logger.info("lastPoint: " + lastPoint);
    return cumulativeDistance;
  }

  public Coordinate findClosestCoordinateOnLine(LineString lineString, Point chargerLocation) {
    Double minDistance = Double.MAX_VALUE;
    Coordinate closestCoordinate = null;

    for (Coordinate coordinate : lineString.getCoordinates()) {
      GeodesicData geodesicData =
          Geodesic.WGS84.Inverse(
              chargerLocation.getY(), chargerLocation.getX(), coordinate.y, coordinate.x);
      Double distance = geodesicData.s12; // dist in meters

      if (distance < minDistance) {
        minDistance = distance;
        closestCoordinate = coordinate;
      }
    }

    return closestCoordinate;
  }

  private List<Charger> findChargersAtIntervals(LinkedHashMap<Charger, Double> sortedChargers, Route route) {

    // TODO: optimise this to pick fastest chargers if reasonably close to interval, potentially make this a param.
    // TODO: Calc charge time
    List<Charger> chargersAtIntervals = new ArrayList<>();
    Double nextTargetDistance = calculateMaxTravelDistance(route);
    Charger closestCharger = null;
    Double closestDistance = Double.MAX_VALUE;
    Double lastChargerDistance = 0.0; // to track the last chargers dist to calc current battery lvls

    logger.info("Total route length: " + route.getRouteLength());

    // stop if charger is beyond the total route length
    for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
      Double chargerDistance = entry.getValue();

      // safety check
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
          route.rechargeBattery(); // recharge to getChargeLevelAfterEachStopPct (defaults to 90%)
          logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
          nextTargetDistance = closestDistance + calculateMaxTravelDistance(route);  // calc next interval distance
          // stop if the next interval is beyond the route end
          if (nextTargetDistance > route.getRouteLength()) break;
          closestCharger = null; // reset for  next interval
          closestDistance = Double.MAX_VALUE;
        }

        // check if the current charger should be considered for next interval
//        if (chargerDistance <= nextTargetDistance) {
//          closestCharger = entry.getKey();
//          closestDistance = chargerDistance;
//        }
      }
    }

    // add the last found closest charger if any and not already added
    if (closestCharger != null && !chargersAtIntervals.contains(closestCharger)) {
      chargersAtIntervals.add(closestCharger);
      double finalSegmentDistance = closestDistance - lastChargerDistance;
      route.setCurrentBattery(route.getCurrentBattery() - (finalSegmentDistance));
      logger.info("Battery at: " + route.getCurrentBattery() + " m");
      route.rechargeBattery();
      logger.info("Recharging battery to: " + route.getCurrentBattery() + " m");
    }

    // final update to battery level at route end
    route.setCurrentBattery(route.getCurrentBattery() - (route.getRouteLength() - closestDistance));

//    for (Charger charger : chargersAtIntervals) {
//      logger.info("Charger Location: " + charger.getLocation());
//    }

    logger.info("Distances for chargers added to chargersAtIntervals:");
    for (Charger charger : chargersAtIntervals) {
      Double distance = sortedChargers.get(charger);
      logger.info("Charger ID: " + charger.getId() + ", Distance: " + distance + " m");
    }
    logger.info("Charge level at end of route: " + route.getCurrentBattery() + " m");

    return chargersAtIntervals;
  }




  public void printChargerDistanceMap(LinkedHashMap<Charger, Double> map) {
    for (Map.Entry<Charger, Double> entry : map.entrySet()) {
      Charger charger = entry.getKey();
      Double distance = entry.getValue();
      System.out.println("Charger ID: " + charger.getId() + ", Distance: " + distance + " m");
    }
  }

}
