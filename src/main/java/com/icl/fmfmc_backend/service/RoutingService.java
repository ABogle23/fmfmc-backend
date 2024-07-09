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

import java.util.Collections;
import java.util.List;
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
    System.out.println("Buffered Polygon: " + bufferedLineString.toText().substring(0, 100) + "...");

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

    List<Charger> suitableChargers =
        findSuitableChargers(route.getLineStringRoute(), chargersWithinPolygon, routeRequest);
    logger.info("Suitable chargers: " + suitableChargers.size());

    // find FoodEstablishments based on buffered LineString
    String tmpPolygonFoursquareFormat = polygonStringToFoursquareFormat(bufferedLineString);
    System.out.println("tmpPolygonFoursquareFormat " + tmpPolygonFoursquareFormat.substring(0, 100) + "...");

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

//  private List<Charger> findSuitableChargers(
//      LineString route, List<Charger> chargers, RouteRequest request) {
//    double maxTravelDistance =
//        calculateMaxTravelDistance(
//            request.getStartingBattery(), request.getMinChargeLevel(), request.getEvRange());
//    logger.info("Max travel distance: " + maxTravelDistance);
//    Point lastPointBeforeRecharge = travelAlongRoute(route, maxTravelDistance);
//    logger.info("Last point before recharge: " + lastPointBeforeRecharge);
//    return chargers.stream()
//        .filter(
//            charger -> charger.getLocation().distance(lastPointBeforeRecharge) <= maxTravelDistance)
//        .collect(Collectors.toList());
//  }

  private double calculateMaxTravelDistance(double startingBattery, double minChargeLevel, double evRange) {
    double usableBattery = startingBattery - minChargeLevel;
    return ((usableBattery / 100) * evRange) * 1000;
  }

//  public Point travelAlongRoute(LineString route, double maxDistance) {
//    double cumulativeDistance = 0;
//    Point lastPoint = (Point) route.getStartPoint();
//
//    for (int i = 1; i < route.getNumPoints(); i++) {
//      Point currentPoint = route.getPointN(i);
//      GeodesicData g = Geodesic.WGS84.Inverse(lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX());
//      double segmentDistance = g.s12;  // distance in meters
//
//      if (i % 10 == 0) {
//        logger.info("Cumulative distance - pt " + i + " : " + cumulativeDistance);
//      }
//
//      if (cumulativeDistance + segmentDistance > maxDistance) {
//        return lastPoint; // last valid point before exceeding maxDistance
//      }
//
//      cumulativeDistance += segmentDistance;
//      lastPoint = currentPoint;
//    }
//
//    return lastPoint;
//  }

  private List<Charger> findSuitableChargers(LineString route, List<Charger> chargers, RouteRequest request) {
    double maxTravelDistance = calculateMaxTravelDistance(request.getStartingBattery(), request.getMinChargeLevel(), request.getEvRange());
    logger.info("Max travel distance: " + maxTravelDistance);

    return chargers.stream()
            .filter(charger -> isChargerWithinTravelDistance(route, charger.getLocation(), maxTravelDistance))
            .collect(Collectors.toList());
  }

  private boolean isChargerWithinTravelDistance(LineString route, Point chargerLocation, double maxDistance) {
    double distanceToCharger = calculateDistanceAlongRouteToNearestPoint(route, chargerLocation);
    logger.info("Distance to charger: " + distanceToCharger);
    return distanceToCharger <= maxDistance;
  }

  private double calculateDistanceAlongRouteToNearestPoint(LineString route, Point chargerLocation) {
    Coordinate[] nearestCoordinatesOnRoute = DistanceOp.nearestPoints(route, chargerLocation);
    Point nearestPointOnRoute = new GeometryFactory().createPoint(nearestCoordinatesOnRoute[0]);
  logger.info("==========Next Charger==========");
    logger.info("Nearest point on route: " + nearestPointOnRoute + " to charger: " + chargerLocation);

    double cumulativeDistance = 0;
    Point lastPoint = (Point) route.getStartPoint();

    for (int i = 1; i < route.getNumPoints(); i++) {
      Point currentPoint = route.getPointN(i);
      GeodesicData g = Geodesic.WGS84.Inverse(lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX());
      double segmentDistance = g.s12; // distance in meters
//      if (i % 100 == 0) {
//        logger.info("Cumulative distance - pt " + i + " : " + cumulativeDistance);
//      }
      if (currentPoint.equalsExact(nearestPointOnRoute,0.005) || i == route.getNumPoints() - 1) {
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


}
