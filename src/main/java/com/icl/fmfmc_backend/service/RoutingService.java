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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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
    System.out.println("Original LineString: " + lineString.toText());
    System.out.println("Buffered Polygon: " + bufferedLineString);

    // Build Route Object with LineString and buffered LineString
    Route route =
        new Route(
            lineString,
            bufferedLineString,
            osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getProperties().getSummary().getDistance(),
            osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getProperties().getSummary().getDuration());



    // convert polyline & polygon to Strings
    String polyline = getPolylineAsString(osrDirectionsServiceGeoJSONResponse);
    String tmpPolygon = PolylineUtility.encodePolygon(bufferedLineString);

    // find chargers based on buffered LineString
    List<Charger> chargersWithinPolygon =
        chargerService.getChargersWithinPolygon(bufferedLineString);
    //    chargersWithinPolygon =
    // chargerService.getChargersByConnectionType(routeRequest.getConnectionTypes());
    //    logger.info("Chargers within query: " + chargersWithinPolygon.size());
    //    chargersWithinPolygon =
    // chargerService.getChargersByChargeSpeed(routeRequest.getMinKwChargeSpeed(),
    // routeRequest.getMaxKwChargeSpeed());
    //    logger.info("Chargers within query: " + chargersWithinPolygon.size());
    //    chargersWithinPolygon =
    // chargerService.getChargersByMinNoChargePoints(routeRequest.getMinNoChargePoints());
    //    logger.info("Chargers within query: " + chargersWithinPolygon.size());
    //    chargersWithinPolygon = chargerService.getChargersWithinRadius(new
    // GeometryFactory().createPoint(new Coordinate(-0.043221, 51.472276)), 1000.0);;
    //    logger.info("Chargers within query: " + chargersWithinPolygon.size());
    //    chargersWithinPolygon =
    //        chargerService.getChargersByParams(
    //            null,
    //                new GeometryFactory().createPoint(new
    // Coordinate(routeRequest.getStartLong(),routeRequest.getStartLat())),
    //            2000.0,
    //            List.of(ConnectionType.CCS, ConnectionType.CHADEMO, ConnectionType.TYPE2),
    //            null,
    //                4,
    //            null);

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
    chargersWithinPolygon = chargerService.getChargersByParams(query);

    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    //    chargersWithinPolygon =
    //            chargerService.findChargersByParam(
    //                    null,
    //                    new GeometryFactory().createPoint(new
    // Coordinate(routeRequest.getStartLong(),routeRequest.getStartLat())),
    //                    2000.0,
    //                    List.of(ConnectionType.CCS, ConnectionType.CHADEMO, ConnectionType.TYPE2),
    //                    null,
    //                    4,
    //                    null);
    //    ;
    //    logger.info("Chargers within query: " + chargersWithinPolygon.size());

    // find FoodEstablishments based on buffered LineString
    String tmpPolygonFoursquareFormat = polygonStringToFoursquareFormat(bufferedLineString);
    System.out.println("tmpPolygonFoursquareFormat " + tmpPolygonFoursquareFormat);

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
            routeRequest,
            polyline,
            tmpPolygon,
            chargersWithinPolygon,
            foodEstablishmentsWithinPolygon);

    return dummyRouteResult;
  }

  private static String getPolylineAsString(
      OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse) {
    List<GeoCoordinates> routeCoordinates =
        osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getGeometry().getCoordinates();
    String polyline = PolylineUtility.encodeGeoCoordinatesToPolyline(routeCoordinates);
    System.out.println("Encoded Polyline: " + polyline);
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
}

// [[-5.527232277582699,50.125797154650854],[-5.230125079140789,50.232877561473444],[-4.78339855527325,50.291757884188414]]
// [[-3.5406341629140705,50.71676536226637],[-3.5473912531536964,50.71491264223052],[-3.54012814556695,50.70548427596927]]
