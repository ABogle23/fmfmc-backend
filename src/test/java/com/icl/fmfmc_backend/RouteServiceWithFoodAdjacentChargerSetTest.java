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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteServiceWithFoodAdjacentChargerSetTest {

  @Mock private DirectionsClientManager directionsClientManager;

  @Mock private ChargerService chargerService;

  @InjectMocks private RoutingService routingService;

  private final RouteRequest routeRequest = TestDataFactory.createDefaultRouteRequest();
  private final DirectionsResponse directionsResponse =
      TestDataFactory.createDefaultDirectionsResponse();

  private final Route route = new Route(directionsResponse, routeRequest);

  private final List<Charger> chargers = TestDataFactory.createChargers();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Food adjacent charger is not added to suitable chargers list")
  public void foodAdjacentChargerIsNotAdded() {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);

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
    TestHelperFunctions.assertEqualsWithTolerance(54599, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Only food adjacent charger is added to suitable chargers list")
  public void onlyFoodAdjacentChargerIsAdded() {

    //        List<Charger> inadequateChargers =
    // TestDataFactory.createChargersForCurrentBatterySetTest();

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Food adjacent charger is out of range")
  public void foodAdjacentChargerIsOutOfRange() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 117934L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 90470L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 60000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
            assertThrows(
                    NoChargerWithinRangeException.class,
                    () -> routingService.findSuitableChargers(route, insufficientChargers),
                    "Expected findSuitableChargers to throw, it didn't");

  }

  @Test
  @DisplayName("Food adjacent charger is out of range")
  public void tbh() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 117934L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 90470L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, route.getCurrentBattery(), 100);
  }


  //

}
