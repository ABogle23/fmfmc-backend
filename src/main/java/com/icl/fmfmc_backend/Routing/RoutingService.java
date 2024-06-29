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
import com.icl.fmfmc_backend.service.OSRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class RoutingService {


    private final OSRService osrService;
    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    private final ChargerService chargerService;

    private final FoodEstablishmentService foodEstablishmentService;
    private final GeometryService geometryService;

    public RouteResult getRoute(RouteRequest routeRequest) {
        logger.info("Fetching route from routing service");

        Double[] start = {routeRequest.getStartLong(), routeRequest.getStartLat()};
        Double[] end = {routeRequest.getEndLong(), routeRequest.getEndLat()};
        List<Double[]> startAndEndCoordinates = List.of(start, end);

        OSRDirectionsServiceGeoJSONRequest osrDirectionsServiceGeoJSONRequest =
                new OSRDirectionsServiceGeoJSONRequest(startAndEndCoordinates);

        System.out.println(osrDirectionsServiceGeoJSONRequest);

        OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse = osrService.getDirectionsGeoJSON(osrDirectionsServiceGeoJSONRequest);


        List<GeoCoordinates> routeCoordinates = osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getGeometry().getCoordinates();
        String polyline = PolylineUtility.encodeGeoCoordinatesToPolyline(routeCoordinates);
        System.out.println("Encoded Polyline: " + polyline);

        List<Charger> chargers = chargerService.getAllChargers();
        List<FoodEstablishment> foodEstablishments =
                foodEstablishmentService.getAllFoodEstablishments().stream()
                        .limit(2)
                        .collect(Collectors.toList());
        RouteResult dummyRouteResult =
                new RouteResult(
                        polyline,
                        100.0,
                        3600.0,
                        chargers,
                        foodEstablishments,
                        //            List.of(new Charger(), new Charger()),
                        //            List.of(new FoodEstablishment(), new FoodEstablishment()),
                        routeRequest);

        return dummyRouteResult;

    }
//
//  // 1. Create a new class called RoutingBuilder in the Routing package
//  // 2. Add a method called buildRoutingRequest that takes a list of coordinates as input and
//  // returns a RoutingRequest object
//  // 3. In the buildRoutingRequest method, create a new RoutingRequest object and set the
//  // coordinates field to the input list of coordinates
//  // 4. Return the RoutingRequest object from the method
//
//  public RoutingRequest buildRoutingRequest(List<List<Double>> coordinates) {
//    RoutingRequest request = new RoutingRequest();
//    request.setCoordinates(coordinates);
//    return request;
//  }
//
//  // 5. Add a method called buildRoutingResponse that takes a RoutingResponse object as input and
//  // returns a list of coordinates
//  // 6. In the buildRoutingResponse method, get the coordinates field from the input RoutingResponse
//  // object and return it
//
//  public List<List<Double>> buildRoutingResponse(RoutingResponse response) {
//    return response.getCoordinates();
//  }
//
//  // 7. Add a method called buildRoutingErrorResponse that takes a RoutingErrorResponse object as
//  // input and returns a string
//  // 8. In the buildRoutingErrorResponse method, get the message field from the input
//  // RoutingErrorResponse object and return it
//
//  public String buildRoutingErrorResponse(RoutingErrorResponse error) {
//    return error.getMessage();
//  }
//
//  // 9. Add a method called buildRoutingService that takes a RoutingRequest object as input and
//  // returns a RoutingResponse object
//  // 10. In the buildRoutingService method, create a new RoutingResponse object and set the
//  // coordinates field to the coordinates from the input RoutingRequest object
//  // 11. Return the RoutingResponse object from the method
//
//  public RoutingResponse buildRoutingService(RoutingRequest request) {
//    RoutingResponse response = new RoutingResponse();
//    response.setCoordinates(request.getCoordinates());
//    return response;
//  }
//
//  // 12. Add a method called buildRoutingErrorService that takes a RoutingRequest object as input
//  // and returns a RoutingErrorResponse object
//  // 13. In the buildRoutingErrorService method, create a new RoutingErrorResponse object and set
//  // the message field to an error message
//  // 14. Return the RoutingErrorResponse object from the method
//
//  public RoutingErrorResponse buildRoutingErrorService(RoutingRequest request) {
//    RoutingErrorResponse error = new RoutingErrorResponse();
//    error.setMessage("Error fetching directions");
//    return error;
//  }

  // 15. Add a

}

// [[-5.527232277582699,50.125797154650854],[-5.230125079140789,50.232877561473444],[-4.78339855527325,50.291757884188414]]
// [[-3.5406341629140705,50.71676536226637],[-3.5473912531536964,50.71491264223052],[-3.54012814556695,50.70548427596927]]
