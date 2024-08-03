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
import org.locationtech.jts.geom.Point;
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

  private final List<Charger> chargers = TestDataFactory.createChargersForBatteryTest();

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
  @DisplayName(
      "Only food adjacent charger from multiple charger list is added to suitable chargers list")
  public void onlyFoodAdjacentChargerIsAddedFromMultipleChargers() {

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
  @DisplayName(
      "Only food adjacent charger from single charger list is added to suitable chargers list")
  public void onlyFoodAdjacentChargerIsAddedAndIsOnlyChargerInList() {

    List<Charger> inadequateChargers = new ArrayList<>();
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 117934L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, inadequateChargers);
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
  @DisplayName(
      "Only food adjacent charger is added despite starting battery being sufficient for entire route")
  public void onlyFoodAdjacentChargerIsAddedDespiteStartingBatteryBeingSufficientForEntireRoute() {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 300000.0, 0.2, 0.9, 0.2);
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
    TestHelperFunctions.assertEqualsWithTolerance(223095, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Food adjacent charger is first in the list is out of range")
  public void foodAdjacentChargerIsFirstInListAndOutOfRange() {

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
  @DisplayName("Food adjacent charger is second in the list is out of range")
  public void foodAdjacentChargerIsSecondInListAndOutOfRange() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 109188L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 90470L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 60000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 90470L);
    // 168555L
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(route, insufficientChargers),
            "Expected findSuitableChargers to throw, it didn't");
  }

  @Test
  @DisplayName("Food adjacent charger is added after charger map loop")
  public void foodAdjacentChargerIsAddedAfterChargerMapLoop() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 75045L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 75045L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(83396, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Food adjacent charger is 2nd in charger list and is added 2nd")
  public void foodAdjacentChargerIsSecondInChargerListAndIsAddedSecond() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Food adjacent charger is 2nd in charger list and is added second and meets final destination charge level")
  public void foodAdjacentChargerIsSecondInChargerListAndIsAddedSecondAndMeetsFinalDestChrgLvl() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 285685L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.75);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Food adjacent charger is 2nd and last in charger list and is added second and meets final destination charge level")
  public void
      foodAdjacentChargerIsSecondAndLastInChargerListAndIsAddedSecondAndMeetsFinalDestChrgLvl() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.75);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Food adjacent charger is 2nd in charger list and is added 2nd and meets final destination charge level with an additional charger")
  public void
      foodAdjacentChargerIsSecondInChargerListAndIsAddedSecondAndFinalDestChrgLvlIsMetWithOtherCharger() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 285685L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.8);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, route, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(route, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 3, 41030L, 258995L, 75045L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(83396, route.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
          "Only food adjacent charger should be added despite it being very close to charger with higher powerKw")
  public void onlyFoodAdjacentChargerIsAddedDespiteBeingHigherPowerKwChargerNearby() {

    TestHelperFunctions.setBatteryAndCharging(route, 0.9, 100000.0, 0.2, 0.9, 0.2);

    List<Charger> chargersWithHigherKwAtSameDist = new ArrayList<>(TestDataFactory.createChargersForBatteryTest());
    Charger highPowerKwCharger = TestDataFactory.createDefaultCharger(9999L, 1, 0.0, 0.0);
    highPowerKwCharger.getConnections().get(0).setPowerKW(350L);
    Point foodAdjacentChargerLocation = TestHelperFunctions.findChargerById(chargers, 117934L).getLocation();
    highPowerKwCharger.setLocation(foodAdjacentChargerLocation);
    chargersWithHigherKwAtSameDist.add(highPowerKwCharger);
    TestHelperFunctions.setFoodAdjacentCharger(chargersWithHigherKwAtSameDist, route, 117934L);


    List<Charger> suitableChargers = null;
    try {
      suitableChargers = routingService.findSuitableChargers(route, chargersWithHigherKwAtSameDist);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(route.getCurrentBattery() > route.getMinChargeLevel());
    assertTrue(route.getCurrentBattery() > route.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, route.getCurrentBattery(), 100);
  }

}
