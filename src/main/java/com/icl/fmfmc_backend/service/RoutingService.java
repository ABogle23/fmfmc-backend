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
    logger.info("Fetching route from routing service");

    // get initial route
    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
        getOsrDirectionsServiceGeoJSONResponse(routeRequest);

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
    Route route =
        new Route(
            lineString,
            bufferedLineString,
            osrDirectionsServiceGeoJSONResponse
                .getFeatures()
                .get(0)
                .getProperties()
                .getSummary()
                .getDistance(),
            osrDirectionsServiceGeoJSONResponse
                .getFeatures()
                .get(0)
                .getProperties()
                .getSummary()
                .getDuration(),
            routeRequest);

    // convert polyline & polygon to Strings
    String polyline = getPolylineAsString(osrDirectionsServiceGeoJSONResponse);
    String tmpPolygon = PolylineUtility.encodePolygon(bufferedLineString);

    // find chargers based on buffered LineString

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

    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    // filter chargers to those reachable from the route based on battery level

    List<Charger> suitableChargers = findSuitableChargers(route, chargersWithinPolygon);
    logger.info("Suitable chargers: " + suitableChargers.size());

    // find FoodEstablishments based on buffered LineString
    String tmpPolygonFoursquareFormat = polygonStringToFoursquareFormat(bufferedLineString);
    System.out.println(
        "tmpPolygonFoursquareFormat " + tmpPolygonFoursquareFormat.substring(0, 100) + "...");

    FoursquareRequest params =
        new FoursquareRequestBuilder()
            .setCategories(routeRequest.getEatingOptions())
            .setPolygon(tmpPolygonFoursquareFormat)
            .createFoursquareRequest();

    //    List<FoodEstablishment> foodEstablishmentsWithinPolygon =
    //        foodEstablishmentService.getFoodEstablishmentsByParam(params);
    List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();

    // build result
    RouteResult dummyRouteResult =
        getRouteResult(
            routeRequest, polyline, tmpPolygon, suitableChargers, foodEstablishmentsWithinPolygon);

    return dummyRouteResult;
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
      List<Charger> chargers,
      List<FoodEstablishment> foodEstablishments) {
    //    List<Charger> chargers = chargerService.getAllChargers();
    //    List<FoodEstablishment> foodEstablishments =
    //        foodEstablishmentService.getAllFoodEstablishments().stream()
    //            .limit(2)
    //            .collect(Collectors.toList());
    RouteResult dummyRouteResult =
        new RouteResult(
            polyline, tmpPolygon, 100.0, 3600.0, chargers, foodEstablishments, routeRequest);
    return dummyRouteResult;
  }

  private OSRDirectionsServiceGeoJSONResponse getOsrDirectionsServiceGeoJSONResponse(
      RouteRequest routeRequest) {
    logger.info("Fetching initial route");
    Double[] start = {routeRequest.getStartLong(), routeRequest.getStartLat()};
    Double[] end = {routeRequest.getEndLong(), routeRequest.getEndLat()};
    List<Double[]> startAndEndCoordinates = List.of(start, end);

    OSRDirectionsServiceGeoJSONRequest osrDirectionsServiceGeoJSONRequest =
        new OSRDirectionsServiceGeoJSONRequest(startAndEndCoordinates);

    System.out.println(osrDirectionsServiceGeoJSONRequest);

    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
        osrClient.getDirectionsGeoJSON(osrDirectionsServiceGeoJSONRequest);
    return osrDirectionsServiceGeoJSONResponse;
  }

  public static String polygonStringToFoursquareFormat(Polygon polygon) {

    int step = 40;
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
    return route.getCurrentBattery() - route.getMinChargeLevel();
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

    List<Charger> chargersAtIntervals = new ArrayList<>();
    Double nextTargetDistance = route.getCurrentBattery();
    Charger closestCharger = null;
    Double closestDistance = Double.MAX_VALUE;

    logger.info("Total route length: " + route.getRouteLength());

    // stop if charger is beyond the total route length
    for (Map.Entry<Charger, Double> entry : sortedChargers.entrySet()) {
      Double chargerDistance = entry.getValue();
      if (chargerDistance > route.getRouteLength()) {
        break;
      }

      if (chargerDistance <= nextTargetDistance) {
        // find closest charger to the target dist without exceeding it
        if (closestCharger == null || Math.abs(nextTargetDistance - chargerDistance) < Math.abs(nextTargetDistance - closestDistance)) {
          closestCharger = entry.getKey();
          closestDistance = chargerDistance;
        }
      } else {
        // once charger distance exceeds the target, add the last closest charger and move to next interval
        if (closestCharger != null) {
          chargersAtIntervals.add(closestCharger);
          closestCharger = null; // reset for  next interval
          closestDistance = Double.MAX_VALUE;

          route.rechargeBattery(1.0); // recharge to full
          logger.info("Recharging to full");
          nextTargetDistance += calculateMaxTravelDistance(route);
          // stop if the next interval is beyond the route end
          if (nextTargetDistance > route.getRouteLength()) break;
        }

        // check if the current charger should be considered for next interval
        if (chargerDistance <= nextTargetDistance) {
          closestCharger = entry.getKey();
          closestDistance = chargerDistance;
        }
      }
    }

    // add the last found closest charger if any
    if (closestCharger != null) {
      chargersAtIntervals.add(closestCharger);
    }

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
