package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.api.JourneyContext;
import com.icl.fmfmc_backend.dto.api.RouteRequest;
import com.icl.fmfmc_backend.dto.api.RouteResult;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.routing.Route;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
import com.icl.fmfmc_backend.entity.enums.FallbackStrategy;
import com.icl.fmfmc_backend.exception.service.*;
import com.icl.fmfmc_backend.util.TestDataFactory;
import com.icl.fmfmc_backend.util.TestHelperFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class JourneyServiceTest {

  @Mock private ChargerService chargerService;

  @Mock private FoodEstablishmentService foodEstablishmentService;

  @Mock private PoiService poiService;

  @Mock private RoutingService routingService;

  @InjectMocks private JourneyService journeyService;

  DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
  RouteRequest routeRequest = TestDataFactory.createDefaultRouteRequest();

  private final Route route = new Route(directionsResponse, routeRequest);

  private final List<Charger> chargers = TestDataFactory.createChargersForBatteryTest();

  private final List<FoodEstablishment> foodEstablishmentLocations =
      TestDataFactory.createFoodEstablishmentsForPoiTest();

  private final JourneyContext journeyContext = new JourneyContext();

  public void setRequestRelatedParams() {
    routeRequest.setStopForEating(true);
    route.setStopForEating(true);
    route.setChargerSearchDeviation(DeviationScope.moderate);
    route.setEatingOptionSearchDeviation(DeviationScope.moderate);
  }

  @Test
  public void getJourneyReturnsRouteResultWhenJourneyFound()
      throws JourneyNotFoundException,
          NoFoodEstablishmentsFoundException,
          NoFoodEstablishmentsInRangeOfChargerException,
          PoiServiceException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    setRequestRelatedParams();

    FoodEstablishment optimalFe =
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.5354433, 51.214881);

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    //    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    Charger adjacentCharger = TestHelperFunctions.findChargerById(chargers, 117934L);
    System.out.println("adjacentCharger: " + adjacentCharger);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(poiService.getFoodEstablishmentOnRoute(any()))
        .thenReturn(Tuples.of(List.of(optimalFe), adjacentCharger));

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenReturn(Arrays.asList(adjacentCharger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenReturn(Arrays.asList(adjacentCharger));

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());

    RouteResult result = null;
    try {
      result = journeyService.getJourney(routeRequest, journeyContext);
    } catch (JourneyNotFoundException e3) {
      fail("JourneyNotFoundException should not have been thrown");
    }

    System.out.println("route.getFoodAdjacentCharger(): " + route.getFoodAdjacentCharger());

    assertNotNull(result);
    assertEquals(optimalFe.getId(), result.getFoodEstablishments().get(0).getId());
    assertEquals(adjacentCharger.getId(), result.getChargers().get(0).getId());

    System.out.println("RouteResult: " + result);
  }

  @Test
  public void getJourneyReturnsRouteResultWithFoodEstablishmentAfterFallback()
      throws JourneyNotFoundException,
          NoFoodEstablishmentsFoundException,
          NoFoodEstablishmentsInRangeOfChargerException,
          PoiServiceException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    setRequestRelatedParams();

    FoodEstablishment optimalFe =
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.5354433, 51.214881);

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    //    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    Charger adjacentCharger = TestHelperFunctions.findChargerById(chargers, 117934L);
    System.out.println("adjacentCharger: " + adjacentCharger);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(poiService.getFoodEstablishmentOnRoute(any()))
        .thenThrow(new NoFoodEstablishmentsFoundException("No food establishments found"))
        .thenReturn(Tuples.of(List.of(optimalFe), adjacentCharger));

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenReturn(Arrays.asList(adjacentCharger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenReturn(Arrays.asList(adjacentCharger));

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());

    RouteResult result = null;
    try {
      result = journeyService.getJourney(routeRequest, journeyContext);
    } catch (JourneyNotFoundException e3) {
      fail("JourneyNotFoundException should not have been thrown");
    }

    System.out.println("route.getFoodAdjacentCharger(): " + route.getFoodAdjacentCharger());

    assertNotNull(result);
    assertEquals(optimalFe.getId(), result.getFoodEstablishments().get(0).getId());
    assertEquals(adjacentCharger.getId(), result.getChargers().get(0).getId());

    assertFallbackStrategies(
        journeyContext, List.of(FallbackStrategy.EXPANDED_EATING_OPTION_SEARCH_AREA));

    System.out.println("RouteResult: " + result);
  }

  @Test
  public void getJourneyReturnsRouteResultWithoutFoodEstablishmentIfNotFound()
      throws JourneyNotFoundException {

    setRequestRelatedParams();

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    //    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    try {
      Mockito.when(poiService.getFoodEstablishmentOnRoute(any()))
          .thenThrow(new NoFoodEstablishmentsFoundException("No food establishments found"));
    } catch (NoFoodEstablishmentsFoundException
        | NoFoodEstablishmentsInRangeOfChargerException
        | PoiServiceException ignored) {
    }

    try {
      Mockito.when(routingService.getChargersOnRoute(any())).thenReturn(Arrays.asList(charger));
    } catch (NoChargersOnRouteFoundException e) {
      fail("NoChargersOnRouteFoundException should not have been thrown");
    }

    try {
      Mockito.when(routingService.findSuitableChargers(any(), any()))
          .thenReturn(Arrays.asList(charger));
    } catch (NoChargerWithinRangeException e2) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());

    RouteResult result = null;
    try {
      result = journeyService.getJourney(routeRequest, journeyContext);
    } catch (JourneyNotFoundException e3) {
      fail("JourneyNotFoundException should not have been thrown");
    }

    assertNotNull(result);
    assertEquals(0, result.getFoodEstablishments().size());
    assertEquals(charger.getId(), result.getChargers().get(0).getId());

    assertFallbackStrategies(
        journeyContext, List.of(FallbackStrategy.SKIPPED_EATING_OPTION_SEARCH));

    System.out.println("RouteResult: " + result);
  }

  @Test
  public void getJourneyAppliesRelaxChargerSearchIfNoChargersFoundOnRoute()
      throws JourneyNotFoundException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenReturn(Arrays.asList(charger));
    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());
    RouteResult result = journeyService.getJourney(routeRequest, journeyContext);

    assertNotNull(result);
    assertEquals(charger.getId(), result.getChargers().get(0).getId());

    assertFallbackStrategies(
        journeyContext,
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE));
  }

  @Test
  public void getJourneyThrowsJourneyNotFoundExceptionIfRelaxChargerSearchFails()
      throws JourneyNotFoundException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"));

    JourneyNotFoundException thrown =
        assertThrows(
            JourneyNotFoundException.class,
            () -> journeyService.getJourney(routeRequest, journeyContext),
            "Expected getJourney to throw, it didn't");

    assertFallbackStrategies(
        journeyContext,
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE));
  }

  @Test
  public void appliesChargerRelaxChargingConstraintsIfThereAreNoSuitableChargersInRange()
      throws JourneyNotFoundException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        //        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());
    RouteResult result = journeyService.getJourney(routeRequest, journeyContext);

    assertNotNull(result);
    assertEquals(charger.getId(), result.getChargers().get(0).getId());
    assertFallbackStrategies(
        journeyContext, List.of(FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS));
  }

  @Test
  public void
      appliesChargerRelaxChargingConstraintsAndRelaxChargerSearchIfThereAreNoSuitableChargersInRange()
          throws JourneyNotFoundException,
              NoChargersOnRouteFoundException,
              NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        //        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());
    RouteResult result = journeyService.getJourney(routeRequest, journeyContext);

    assertNotNull(result);
    assertEquals(charger.getId(), result.getChargers().get(0).getId());

    assertFallbackStrategies(
        journeyContext,
        List.of(
            FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS,
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE));
  }

  @Test
  public void
      appliesRelaxChargerSearchIfNoChargersFoundOnRouteThenRelaxChargerSearchIfThereAreNoSuitableChargersInRange()
          throws JourneyNotFoundException,
              NoChargersOnRouteFoundException,
              NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.snapRouteToStops(any(), any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse().getLineString());
    RouteResult result = journeyService.getJourney(routeRequest, journeyContext);

    assertNotNull(result);
    assertEquals(charger.getId(), result.getChargers().get(0).getId());

    assertFallbackStrategies(
        journeyContext,
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE,
            FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS));
  }

  @Test
  public void appliesAllFallbacksAndThrowsJourneyNotFoundException()
      throws JourneyNotFoundException,
          NoChargersOnRouteFoundException,
          NoChargerWithinRangeException {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    Charger charger = TestHelperFunctions.findChargerById(chargers, 117934L);

    Mockito.when(routingService.getDirections(any()))
        .thenReturn(TestDataFactory.createDefaultDirectionsResponse());

    Mockito.when(routingService.getChargersOnRoute(any()))
        .thenThrow(new NoChargersOnRouteFoundException("No chargers found on route"))
        .thenReturn(Arrays.asList(charger));

    Mockito.when(routingService.findSuitableChargers(any(), any()))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"))
        .thenThrow(new NoChargerWithinRangeException("No chargers found within range"));

    JourneyNotFoundException thrown =
        assertThrows(
            JourneyNotFoundException.class,
            () -> journeyService.getJourney(routeRequest, journeyContext),
            "Expected getJourney to throw, it didn't");
    assertFallbackStrategies(
        journeyContext,
        List.of(
            FallbackStrategy.EXPANDED_CHARGER_SEARCH_AREA,
            FallbackStrategy.EXPANDED_CHARGER_SPEED_RANGE,
            FallbackStrategy.RELAXED_CHARGING_CONSTRAINTS));
  }

  private void assertFallbackStrategies(
      JourneyContext journeyContext, List<FallbackStrategy> strategies) {
    for (FallbackStrategy strategy : strategies) {
      assertTrue(journeyContext.getFallbackStrategies().contains(strategy));
    }
  }
}
