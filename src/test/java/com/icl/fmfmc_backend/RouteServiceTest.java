package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.exception.NoChargerWithinRangeException;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.DirectionsClientManager;
import com.icl.fmfmc_backend.service.RoutingService;
import com.icl.fmfmc_backend.util.TestDataFactory;
import com.icl.fmfmc_backend.util.TestHelperFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteServiceTest {

  @Mock private DirectionsClientManager directionsClientManager;

  @Mock private ChargerService chargerService;

  @InjectMocks private RoutingService routingService;

  private final RouteRequest routeRequest = TestDataFactory.createDefaultRouteRequest();
  private final DirectionsResponse directionsResponse =
      TestDataFactory.createDefaultDirectionsResponse();

  private final Route route = new Route(directionsResponse, routeRequest);

  private final List<Charger> chargers = TestDataFactory.createChargersForBatteryTest();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Current battery should be set correctly for the route")
  public void currentBatteryShouldBeSetCorrectly() {

    List<Charger> inadequateChargers = TestDataFactory.createChargersForCurrentBatterySetTest();

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 100000.0, 0.3, 0.9, 0.5);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, inadequateChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 168555L, 258995L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75572, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Range > route length thus requiring no charger")
  public void shouldNotFindACharger() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 200000.0, 0.2, 0.9, 0.2);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      throw new RuntimeException(e);
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 0);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(
        0.9 * 200000.0 - route.getRouteLength(), route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Should find single suitable charger on route successfully")
  public void shouldFindSingleChargersOnRouteSuccessfully() {

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 41030L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery = 90000.0 - (route.getRouteLength() - 55586.0);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Should find multiple suitable charger on route successfully")
  public void shouldFindMultipleChargersOnRouteSuccessfully() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 35000.0, 0.2, 0.9, 0.2);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 4, 109188L, 72601L, 41030L, 285685L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 78644.0);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Should find two suitable chargers on route considering final destination charge level")
  public void shouldFindTwoChargersOnRouteConsideringFinalDestinationChargeLevel() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 100000.0, 0.2, 0.9, 0.7);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 90205L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 73646);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Should find two suitable chargers on route and partially fulfill final destination charge level")
  public void shouldFindTwoChargersOnRoutePartiallyFulfillFinalDestinationChargeLevel() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 100000.0, 0.2, 0.9, 0.9);
    Double PartialFinalDestinationChargeLevelTolerance = 10000.0;
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 75045L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertFalse(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    assertTrue(
        (route.getCurrentBattery() + PartialFinalDestinationChargeLevelTolerance)
            > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 84383);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Should find multiple suitable charger on route considering final destination charge level")
  public void shouldFindMultipleChargersOnRouteConsideringFinalDestinationChargeLevel() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 35000.0, 0.2, 0.9, 0.7);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 5, 109188L, 72601L, 41030L, 285685L, 75045L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 84383);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Should find multiple suitable charger on route Partially fulfilling final destination charge level")
  public void shouldFindMultipleChargersOnRoutePartiallyFulfillingFinalDestinationChargeLevel() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 35000.0, 0.2, 0.9, 0.8);
    Double PartialFinalDestinationChargeLevelTolerance = route.getEvRange() * 0.1;
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 5, 109188L, 72601L, 41030L, 285685L, 75045L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertFalse(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    assertTrue(
        (route.getCurrentBattery() * PartialFinalDestinationChargeLevelTolerance)
            > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 84383);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Should throw NoChargerWithinRangeException for unsuitable route")
  public void shouldThrowExceptionForUnsuitableRoute() {

    TestHelperFunctions.setBatteryAndCharging(route,0.9, 15000.0, 0.2, 0.9, 0.2);
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(route, chargers),
            "Expected findSuitableChargers to throw, it didn't");
    //    assertTrue(thrown.getMessage().contains("specific part of the expected message"));

  }

  @Test
  @DisplayName(
      "Should throw NoChargerWithinRangeException for unsuitable route due to MinChargeLevel")
  public void shouldThrowExceptionForDueToMinChargeLevel() {

    List<Charger> inadequateChargers = TestDataFactory.createChargersForCurrentBatterySetTest();

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.65, 0.9, 0.2);
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(route, inadequateChargers),
            "Expected findSuitableChargers to throw, it didn't");
    //    assertTrue(thrown.getMessage().contains("specific part of the expected message"));

  }

  @Test
  @DisplayName(
      "Should not throw NoChargerWithinRangeException for unsuitable route due to MinChargeLevel")
  public void shouldNotThrowExceptionForDueToMinChargeLevel() {

    List<Charger> inadequateChargers = TestDataFactory.createChargersForCurrentBatterySetTest();

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.4, 0.9, 0.5);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, inadequateChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 168555L, 258995L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery =
        (route.getChargeLevelAfterEachStop()) - (route.getRouteLength() - 76560);
    TestHelperFunctions.assertEqualsWithTolerance(
        expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Should throw NoChargerWithinRangeException during charger map loop due to MinChargeLevel")
  public void shouldThrowExceptionDuringChargerLoopIfNoChargerInRange() {

    List<Charger> inadequateChargers =
        new java.util.ArrayList<>(TestDataFactory.createChargersForCurrentBatterySetTest());
    inadequateChargers.add(chargers.get(4));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.60, 0.9, 0.5);
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(route, inadequateChargers),
            "Expected findSuitableChargers to throw, it didn't");
    //    assertTrue(thrown.getMessage().contains("specific part of the expected message"));

  }

  @Test
  @DisplayName(
      "Should throw NoChargerWithinRangeException when exiting charger map loop if no charger in range")
  public void shouldAlsoThrowExceptionWhenExitingChargerLoopIfNoChargerInRange() {

    List<Charger> inadequateChargers =
        new java.util.ArrayList<>(TestDataFactory.createChargersForCurrentBatterySetTest());

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.60, 0.9, 0.5);
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(route, inadequateChargers),
            "Expected findSuitableChargers to throw, it didn't");
    //    assertTrue(thrown.getMessage().contains("specific part of the expected message"));

  }

  /* Assisting functions */

}
