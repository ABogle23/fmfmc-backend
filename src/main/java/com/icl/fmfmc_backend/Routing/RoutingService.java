package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.OSRDirectionsServiceGeoJSONRequest;
import com.icl.fmfmc_backend.dto.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.dto.RouteRequest;
import com.icl.fmfmc_backend.dto.RouteResult;
import com.icl.fmfmc_backend.entity.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import com.icl.fmfmc_backend.Integration.OSRClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    Polygon bufferedLineString = geometryService.bufferLineString(lineString, 0.009); // 500m is 0.0045
    System.out.println("Original LineString: " + lineString.toText());
    System.out.println("Buffered Polygon: " + bufferedLineString.toText());

    // find chargers based on buffered LineString
    List<Charger> chargersWithinPolygon = chargerService.getChargersWithinPolygon(bufferedLineString);

    // build result
    String polyline = getPolylineAsString(osrDirectionsServiceGeoJSONResponse);
    String tmpPolygon = PolylineUtility.encodePolygon(bufferedLineString);

    RouteResult dummyRouteResult = getRouteResult(routeRequest, polyline, tmpPolygon, chargersWithinPolygon);

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

  private RouteResult getRouteResult(RouteRequest routeRequest, String polyline, String tmpPolygon, List<Charger> chargers) {
//    List<Charger> chargers = chargerService.getAllChargers();
    List<FoodEstablishment> foodEstablishments =
        foodEstablishmentService.getAllFoodEstablishments().stream()
            .limit(2)
            .collect(Collectors.toList());
    RouteResult dummyRouteResult =
        new RouteResult(
            polyline,
            tmpPolygon,
            100.0,
            3600.0,
            chargers,
            foodEstablishments,
            //            List.of(new Charger(), new Charger()),
            //            List.of(new FoodEstablishment(), new FoodEstablishment()),
            routeRequest);
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
}

// [[-5.527232277582699,50.125797154650854],[-5.230125079140789,50.232877561473444],[-4.78339855527325,50.291757884188414]]
// [[-3.5406341629140705,50.71676536226637],[-3.5473912531536964,50.71491264223052],[-3.54012814556695,50.70548427596927]]
