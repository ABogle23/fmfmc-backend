package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.OSRClient;
import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONRequest;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class JourneyService {

    private final OSRClient osrClient;
    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    private final PoiService poiService;
    private final RoutingService routingService;

    public RouteResult getJourney(RouteRequest routeRequest) {
        logger.info("Getting journey");

        // get initial route

        Double[] startCoordinates = {routeRequest.getStartLong(), routeRequest.getStartLat()};
        Double[] endCoordinates = {routeRequest.getEndLong(), routeRequest.getEndLat()};
        List<Double[]> startAndEndCoordinates = List.of(startCoordinates, endCoordinates);
        OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse =
                routingService.getOsrDirectionsServiceGeoJSONResponse(startAndEndCoordinates);

        // Build Route Object
        Route route = new Route(osrDirectionsServiceGeoJSONResponse, routeRequest);


        /* -----Find ideal FoodEstablishment via PoiService----- */

        if (route.getStopForEating()) {
            Tuple2<List<FoodEstablishment>, Charger> poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(route);

            route.setFoodEstablishments(poiServiceTestResults.getT1());
            route.setFoodAdjacentCharger(poiServiceTestResults.getT2());
            LineString routeSnappedToFoodAdjacentCharger = routingService.snapRouteToStops(route, List.of(route.getFoodAdjacentCharger()));
            route.setWorkingLineStringRoute(routeSnappedToFoodAdjacentCharger);
            Polygon bufferedLineString =
                    GeometryService.bufferLineString(routeSnappedToFoodAdjacentCharger, 0.009); // 500m is 0.0045
            route.setBufferedLineString(bufferedLineString);
        } else {
            logger.info("No eating stop requested, skipping food establishment search");
        }


        /* -----Find ideal Route and Chargers via RoutingService----- */

        // get chargers on route
        List<Charger> chargersOnRoute = routingService.getChargersOnRoute(route);

        // filter chargers to those reachable on route based on battery level
        List<Charger> suitableChargers = routingService.findSuitableChargers(route, chargersOnRoute);
        route.setChargersOnRoute(suitableChargers);

        // snap route to optimal Chargers and FoodEstablishments
        LineString routeSnappedToStops = routingService.snapRouteToStops(route, suitableChargers);
        route.setFinalSnappedToStopsRoute(routeSnappedToStops);

        /* -----Build Result----- */

        List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();
        RouteResult routeResult = new RouteResult(route, routeRequest);

        return routeResult;
    }

    /* Assisting Functions */




}