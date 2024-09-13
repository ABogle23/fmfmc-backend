package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.exception.service.*;
import com.icl.fmfmc_backend.geometry_service.GeometryService;
import com.icl.fmfmc_backend.controller.JourneyController;
import com.icl.fmfmc_backend.dto.api.JourneyContext;
import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.dto.api.JourneyResult;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.enums.FallbackStrategy;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import com.icl.fmfmc_backend.util.LogMessages;
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

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
  private static final Logger fileLogger =
      LoggerFactory.getLogger("com.icl.fmfmc_backend.service.JourneyService");
  private final PoiService poiService;
  private final RoutingService routingService;

  /**
   * Retrieves a journey based on the given route request and context.
   *
   * @param journeyRequest the route request object containing start and end coordinates
   * @param context the journey context object containing additional parameters
   * @return the route result object containing the journey details
   * @throws JourneyNotFoundException if no valid journey could be found
   */
  @LogExecutionTime(message = LogMessages.GET_JOURNEY)
  public JourneyResult getJourney(JourneyRequest journeyRequest, JourneyContext context)
      throws JourneyNotFoundException {
    logger.info("Getting journey");
    fileLogger.info("JourneyRequest object: {}", journeyRequest.toString());

    // get initial route

    Double[] startCoordinates = {journeyRequest.getStartLong(), journeyRequest.getStartLat()};
    Double[] endCoordinates = {journeyRequest.getEndLong(), journeyRequest.getEndLat()};
    List<Double[]> startAndEndCoordinates = List.of(startCoordinates, endCoordinates);
    DirectionsResponse directionsResponse = routingService.getDirections(startAndEndCoordinates);

    // Build Route Object
    Journey journey = new Journey(directionsResponse, journeyRequest);
    fileLogger.info("Route object: {}", journey.toString());
    fileLogger.info("DirectionsResponse object: {}", directionsResponse.toString());

    /* -----Find ideal FoodEstablishment via PoiService----- */

    if (journey.getStopForEating()) {
      Tuple2<List<FoodEstablishment>, Charger> poiServiceTestResults = null;
      try {
        poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(journey);
      } catch (PoiServiceException pe) {
        logger.error(
            "Error occurred while fetching food establishments from PoiService: {}",
            pe.getMessage());
      } catch (NoFoodEstablishmentsFoundException
          | NoFoodEstablishmentsInRangeOfChargerException e1) {
        expandFoodEstablishmentSearch(journey, context);
        logger.warn(
            "{}, increasing search range to {}km, retrying...",
            e1.getMessage(),
            journey.getEatingOptionSearchDeviationAsFraction());
        try {
          poiServiceTestResults = poiService.getFoodEstablishmentOnRoute(journey);
        } catch (NoFoodEstablishmentsFoundException
            | NoFoodEstablishmentsInRangeOfChargerException
            | PoiServiceException e3) {
          //                            skipEatingOptionService(route, context);
          logger.error(
              "No food establishments found within range of charger, route will be returned without eating stop");
        }
      }

      if (poiServiceTestResults != null) {
        journey.setFoodEstablishments(poiServiceTestResults.getT1());
        journey.setFoodAdjacentCharger(poiServiceTestResults.getT2());
        for (FoodEstablishment fe : journey.getFoodEstablishments()) {
          fe.setAdjacentChargerId(journey.getFoodAdjacentCharger().getId());
        }
        LineString routeSnappedToFoodAdjacentCharger =
            routingService.snapRouteToStops(journey, List.of(journey.getFoodAdjacentCharger()));
        journey.setWorkingLineStringRoute(routeSnappedToFoodAdjacentCharger);
        Polygon bufferedLineString =
            GeometryService.bufferLineString(
                routeSnappedToFoodAdjacentCharger, 0.009); // 500m is 0.0045
        journey.setBufferedLineString(bufferedLineString);
      } else {
        skipEatingOptionService(journey, context);
        logger.error("Route will be returned without eating stop");
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
      chargersOnRoute = routingService.getChargersOnRoute(journey);
    } catch (NoChargersOnRouteFoundException e) {
      logger.warn("No chargers found on route, relaxing charger search criteria and retrying...");
      relaxChargerSearch(journey, context);
      chargerSearchRelaxed = true;
      try {
        chargersOnRoute = routingService.getChargersOnRoute(journey);
      } catch (NoChargersOnRouteFoundException ex) {
        logger.error("No valid journey could be found.");
        throw new JourneyNotFoundException("No valid journey could be found.");
      }
    }

    // filter chargers to those reachable on route based on battery level
    List<Charger> suitableChargers = null;
    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargersOnRoute);
    } catch (NoChargerWithinRangeException e) {
      logger.warn("No chargers found within range, relaxing charging constraints and retrying...");
      resetRouteSearch(journey);
      relaxChargingConstraints(journey, context);
      try {
        suitableChargers = routingService.findSuitableChargers(journey, chargersOnRoute);
      } catch (NoChargerWithinRangeException ex) {
        if (!chargerSearchRelaxed) {
          logger.warn(
              "No chargers found within range, relaxing charger search criteria and retrying...");
          resetRouteSearch(journey);
          relaxChargerSearch(journey, context);
          try {
            chargersOnRoute = routingService.getChargersOnRoute(journey);
            suitableChargers = routingService.findSuitableChargers(journey, chargersOnRoute);
          } catch (NoChargerWithinRangeException | NoChargersOnRouteFoundException exc) {
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

    journey.setChargersOnRoute(suitableChargers);

    // snap route to optimal Chargers and FoodEstablishments
    LineString routeSnappedToStops = routingService.snapRouteToStops(journey, suitableChargers);
    journey.setFinalSnappedToStopsRoute(routeSnappedToStops);

    // set time schedule in segments object
    journey.setTimes();
    journey.setStartAndEndBatteryLevels();

    /* -----Build Result----- */

//    List<FoodEstablishment> foodEstablishmentsWithinPolygon = Collections.emptyList();
    JourneyResult journeyResult = new JourneyResult(journey, journeyRequest);
    fileLogger.info("JourneyResult object: {}", journeyResult.toString());

    return journeyResult;
  }

  private static void resetRouteSearch(Journey journey) {
    journey.resetBatteryLevel();
    journey.clearChargersOnRoute();
    journey.resetSegmentDetails();
  }

  private void skipEatingOptionService(Journey journey, JourneyContext context) {
    journey.setStopForEating(false);
    context.addFallbackStrategies(List.of(FallbackStrategy.SKIPPED_EATING_OPTION_SEARCH));
  }

  private void relaxChargingConstraints(Journey journey, JourneyContext context) {
    journey.relaxChargingRange();
    context.addFallbackStrategies(List.of(FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS));
  }

  private void relaxChargerSearch(Journey journey, JourneyContext context) {
    journey.expandChargerSearchDeviation();
    journey.expandChargeSpeedRange();
    context.addFallbackStrategies(
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE));
  }

  /* Assisting Functions */
  private void expandFoodEstablishmentSearch(Journey journey, JourneyContext context) {
    // TODO: make context specific
    journey.expandEatingOptionSearchDeviation();
    journey.expandStoppingRange();
    journey.expandFoodCategorySearch();
    journey.expandPriceRange();

    context.addFallbackStrategies(
        List.of(
            FallbackStrategy.EXPANDED_EATING_OPTION_SEARCH_AREA,
            FallbackStrategy.EXPANDED_EATING_OPTION_STOPPING_RANGE,
            FallbackStrategy.EXPANDED_EATING_OPTION_CATEGORY_SEARCH,
            FallbackStrategy.EXPANDED_EATING_OPTION_PRICE_RANGE));
  }
}
