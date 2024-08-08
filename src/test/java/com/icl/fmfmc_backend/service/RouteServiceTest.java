package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.api.RouteRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.routing.Route;
import com.icl.fmfmc_backend.exception.integration.DirectionsClientException;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.exception.service.NoChargerWithinRangeException;
import com.icl.fmfmc_backend.util.TestDataFactory;
import com.icl.fmfmc_backend.util.TestHelperFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.3, 0.9, 0.5);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 200000.0, 0.2, 0.9, 0.2);

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
  @DisplayName("Should select higher power charger if multiple chargers are very close")
  public void shouldFindHighestPowerKwChargerOnRouteSuccessfully() {

    List<Charger> chargersWithHigherKwAtSameDist = new ArrayList<>(TestDataFactory.createChargersForBatteryTest());
    Charger highPowerKwCharger = TestDataFactory.createDefaultCharger(9999L, 1, 0.0, 0.0);
    highPowerKwCharger.getConnections().get(0).setPowerKW(350L);
    // prior to setting the powerKw, the charger with id 41030L has the highest powerKw.
    Point expectedChargerLocation = TestHelperFunctions.findChargerById(chargersWithHigherKwAtSameDist, 41030L).getLocation();
    highPowerKwCharger.setLocation(expectedChargerLocation);
    chargersWithHigherKwAtSameDist.add(highPowerKwCharger);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargersWithHigherKwAtSameDist);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 9999L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    Double expectedFinalBattery = 90000.0 - (route.getRouteLength() - 55586.0);
    TestHelperFunctions.assertEqualsWithTolerance(
            expectedFinalBattery, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Should find multiple suitable charger on route successfully")
  public void shouldFindMultipleChargersOnRouteSuccessfully() {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 35000.0, 0.2, 0.9, 0.2);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(
        suitableChargers, 4, 109188L, 72601L, 41030L, 285685L);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.7);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.9);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 35000.0, 0.2, 0.9, 0.7);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(
        suitableChargers, 5, 109188L, 72601L, 41030L, 285685L, 75045L);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 35000.0, 0.2, 0.9, 0.8);
    Double PartialFinalDestinationChargeLevelTolerance = route.getEvRange() * 0.1;
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(
        suitableChargers, 5, 109188L, 72601L, 41030L, 285685L, 75045L);
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

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 15000.0, 0.2, 0.9, 0.2);
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

  /* Generic Tests */

  @Test
  public void snapRouteToStopsSuccessfully()
      throws JourneyNotFoundException, DirectionsClientException {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, -2.110748, 51.107673);
    Charger charger2 = TestDataFactory.createDefaultCharger(1L, 3, -1.117859, 51.460065);

    List<Charger> chargers = List.of(charger1, charger2);
    DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
    when(directionsClientManager.getDirections(any(DirectionsRequest.class)))
        .thenReturn(directionsResponse);

    LineString result = routingService.snapRouteToStops(route, chargers);

    assertNotNull(result);
    verify(directionsClientManager, times(1)).getDirections(any(DirectionsRequest.class));
  }

  @Test
  public void snapRouteToStopsThrowsExceptionWhenNoDirectionsFound()
      throws DirectionsClientException {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, -2.110748, 51.107673);
    Charger charger2 = TestDataFactory.createDefaultCharger(1L, 3, -1.117859, 51.460065);

    List<Charger> chargers = List.of(charger1, charger2);    when(directionsClientManager.getDirections(any(DirectionsRequest.class)))
        .thenThrow(new DirectionsClientException("No directions found"));

    assertThrows(
        JourneyNotFoundException.class, () -> routingService.snapRouteToStops(route, chargers));
    verify(directionsClientManager, times(1)).getDirections(any(DirectionsRequest.class));
  }

  /* Assisting functions */

}
