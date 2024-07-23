package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.OsrDirectionsClient;
import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.controller.JourneyController;
import com.icl.fmfmc_backend.dto.Api.JourneyContext;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.entity.enums.FallbackStrategy;
import com.icl.fmfmc_backend.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class JourneyService {

  private final OsrDirectionsClient osrDirectionsClient;
  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
  private static final Logger fileLogger =
      LoggerFactory.getLogger("com.icl.fmfmc_backend.service.JourneyService");
  private final PoiService poiService;
  private final RoutingService routingService;

  public RouteResult getJourney(RouteRequest routeRequest, JourneyContext context)
      throws JourneyNotFoundException {
    logger.info("Getting journey");
    fileLogger.info("RouteRequest object: {}", routeRequest.toString());

    // get initial route

    Double[] startCoordinates = {routeRequest.getStartLong(), routeRequest.getStartLat()};
    Double[] endCoordinates = {routeRequest.getEndLong(), routeRequest.getEndLat()};
    List<Double[]> startAndEndCoordinates = List.of(startCoordinates, endCoordinates);
    DirectionsResponse directionsResponse = routingService.getDirections(startAndEndCoordinates);

    // Build Route Object
    Route route = new Route(directionsResponse, routeRequest);
    fileLogger.info("Route object: {}", route.toString());
    fileLogger.info("DirectionsResponse object: {}", directionsResponse.toString());

    /* -----Find ideal FoodEstablishment via PoiService----- */

    if (route.getStopForEating()) {
      Tuple2<List<FoodEstablishment>, Charger> poiServiceTestResults = null;
      try {
        poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(route);
      } catch (NoFoodEstablishmentsFoundException
          | NoFoodEstablishmentsInRangeofChargerException e1) {
        expandFoodEstablishmentSearch(route, context);
        logger.warn(
            "{}, increasing search range to {}km, retrying...",
            e1.getMessage(),
            route.getEatingOptionSearchDeviationAsFraction());
        //                try {
        //                    poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(route);
        //                } catch (NoFoodEstablishmentsFoundException |
        // NoFoodEstablishmentsInRangeofChargerException e2) {
        //                    route.setStopForEating(false);
        //                    logger.error("No food establishments found within range of charger,
        // route will be returned without eating stop");
        //                }
      }

      if (poiServiceTestResults != null) {
        route.setFoodEstablishments(poiServiceTestResults.getT1());
        route.setFoodAdjacentCharger(poiServiceTestResults.getT2());
        LineString routeSnappedToFoodAdjacentCharger =
            routingService.snapRouteToStops(route, List.of(route.getFoodAdjacentCharger()));
        route.setWorkingLineStringRoute(routeSnappedToFoodAdjacentCharger);
        Polygon bufferedLineString =
            GeometryService.bufferLineString(
                routeSnappedToFoodAdjacentCharger, 0.009); // 500m is 0.0045
        route.setBufferedLineString(bufferedLineString);
      }
    } else {
      logger.info("No eating stop requested, skipping food establishment search");
    }

    /* -----Find ideal Route and Chargers via RoutingService----- */

    Boolean chargerSearchRelaxed = false;
    Boolean chargingConstraintsRelaxed = false;

    // get chargers on route
    List<Charger> chargersOnRoute = null;
    try {
      chargersOnRoute = routingService.getChargersOnRoute(route);
    } catch (NoChargersOnRouteFoundException e) {
      logger.warn("No chargers found on route, relaxing charger search criteria and retrying...");
      relaxChargerSearch(route, context);
      chargerSearchRelaxed = true;
      try {
        chargersOnRoute = routingService.getChargersOnRoute(route);
      } catch (NoChargersOnRouteFoundException ex) {
        logger.error("No valid journey could be found.");
        throw new JourneyNotFoundException("No valid journey could be found.");
      }
    }

    // filter chargers to those reachable on route based on battery level
    List<Charger> suitableChargers = null;
    try {
      suitableChargers = routingService.findSuitableChargers(route, chargersOnRoute);
    } catch (NoChargerWithinRangeException e) {
      logger.warn("No chargers found within range, relaxing charging constraints and retrying...");
      resetRouteSearch(route);
      relaxChargingConstraints(route, context);
      try {
        suitableChargers = routingService.findSuitableChargers(route, chargersOnRoute);
      } catch (NoChargerWithinRangeException ex) {
        if (!chargerSearchRelaxed) {
          logger.warn(
              "No chargers found within range, relaxing charger search criteria and retrying...");
          resetRouteSearch(route);
          relaxChargerSearch(route, context);
          try {
            suitableChargers = routingService.findSuitableChargers(route, chargersOnRoute);
          } catch (NoChargerWithinRangeException exc) {
            logger.error("No valid journey could be found despite relaxing request constraints.");
            throw new JourneyNotFoundException(
                "No valid journey could be found despite relaxing request constraints.");
          }
        } else {
          logger.error("No valid journey could be found. despite relaxing request constraints.");
          throw new JourneyNotFoundException(
              "No valid journey could be found despite relaxing request constraints.");
        }
      }
    }

    route.setChargersOnRoute(suitableChargers);

    // snap route to optimal Chargers and FoodEstablishments
    LineString routeSnappedToStops = routingService.snapRouteToStops(route, suitableChargers);
    route.setFinalSnappedToStopsRoute(routeSnappedToStops);

    /* -----Build Result----- */

    List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();
    RouteResult routeResult = new RouteResult(route, routeRequest);
    fileLogger.info("RouteResult object: {}", routeResult.toString());

    return routeResult;
  }

  private static void resetRouteSearch(Route route) {
    route.resetBatteryLevel();
    route.clearChargersOnRoute();
  }

  private void relaxChargingConstraints(Route route, JourneyContext context) {
    route.relaxChargingRange();
    context.addFallbackStrategies(List.of(FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS));
  }

  private void relaxChargerSearch(Route route, JourneyContext context) {
    route.expandChargerSearchDeviation();
    route.expandChargeSpeedRange();
    context.addFallbackStrategies(
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE));
  }

  /* Assisting Functions */
  private void expandFoodEstablishmentSearch(Route route, JourneyContext context) {
    // TODO: make context specific
    route.expandEatingOptionSearchDeviation();
    route.expandStoppingRange();
    route.expandFoodCategorySearch();
    route.expandPriceRange();

    context.addFallbackStrategies(
        List.of(
            FallbackStrategy.EXPANDED_EATING_OPTION_SEARCH_AREA,
            FallbackStrategy.EXPANDED_EATING_OPTION_STOPPING_RANGE,
            FallbackStrategy.EXPANDED_EATING_OPTION_CATEGORY_SEARCH,
            FallbackStrategy.EXPANDED_EATING_OPTION_PRICE_RANGE));
  }
}
