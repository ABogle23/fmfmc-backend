package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.exception.service.NoChargerWithinRangeException;
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

public class RoutingServiceWithFoodAdjacentChargerSetTest {

  @Mock private DirectionsClientManager directionsClientManager;

  @Mock private ChargerService chargerService;

  @InjectMocks private RoutingService routingService;

  private final JourneyRequest journeyRequest = TestDataFactory.createDefaultJourneyRequest();
  private final DirectionsResponse directionsResponse =
      TestDataFactory.createDefaultDirectionsResponse();

  private final Journey journey = new Journey(directionsResponse, journeyRequest);

  private final List<Charger> chargers = TestDataFactory.createChargersForBatteryTest();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Food adjacent charger is not added to suitable chargers list")
  public void foodAdjacentChargerIsNotAdded() {

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 41030L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(54599, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Only food adjacent charger from multiple charger list is added to suitable chargers list")
  public void onlyFoodAdjacentChargerIsAddedFromMultipleChargers() {

    //        List<Charger> inadequateChargers =
    // TestDataFactory.createChargersForCurrentBatterySetTest();

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Only food adjacent charger from single charger list is added to suitable chargers list")
  public void onlyFoodAdjacentChargerIsAddedAndIsOnlyChargerInList() {

    List<Charger> inadequateChargers = new ArrayList<>();
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 117934L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, inadequateChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Only food adjacent charger is added despite starting battery being sufficient for entire route")
  public void onlyFoodAdjacentChargerIsAddedDespiteStartingBatteryBeingSufficientForEntireRoute() {

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 300000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(223095, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Food adjacent charger is first in the list is out of range")
  public void foodAdjacentChargerIsFirstInListAndOutOfRange() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 117934L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 90470L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 60000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 117934L);
    // 168555L
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(journey, insufficientChargers),
            "Expected findSuitableChargers to throw, it didn't");
  }

  @Test
  @DisplayName("Food adjacent charger is second in the list is out of range")
  public void foodAdjacentChargerIsSecondInListAndOutOfRange() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 109188L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 90470L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 60000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 90470L);
    // 168555L
    List<Charger> suitableChargers = null;

    NoChargerWithinRangeException thrown =
        assertThrows(
            NoChargerWithinRangeException.class,
            () -> routingService.findSuitableChargers(journey, insufficientChargers),
            "Expected findSuitableChargers to throw, it didn't");
  }

  @Test
  @DisplayName("Food adjacent charger is added after charger map loop")
  public void foodAdjacentChargerIsAddedAfterChargerMapLoop() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 75045L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 75045L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(83396, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Food adjacent charger is 2nd in charger list and is added 2nd")
  public void foodAdjacentChargerIsSecondInChargerListAndIsAddedSecond() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, journey.getCurrentBattery(), 100);
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

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.75);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
      "Food adjacent charger is 2nd and last in charger list and is added second and meets final destination charge level")
  public void
      foodAdjacentChargerIsSecondAndLastInChargerListAndIsAddedSecondAndMeetsFinalDestChrgLvl() {

    List<Charger> insufficientChargers = new ArrayList<>();
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    insufficientChargers.add(TestHelperFunctions.findChargerById(chargers, 258995L));

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.75);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 2, 41030L, 258995L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(75573, journey.getCurrentBattery(), 100);
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

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.8);
    TestHelperFunctions.setFoodAdjacentCharger(chargers, journey, 258995L);

    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, insufficientChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 3, 41030L, 258995L, 75045L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(83396, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName(
          "Only food adjacent charger should be added despite it being very close to charger with higher powerKw")
  public void onlyFoodAdjacentChargerIsAddedDespiteBeingHigherPowerKwChargerNearby() {

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 100000.0, 0.2, 0.9, 0.2);

    List<Charger> chargersWithHigherKwAtSameDist = new ArrayList<>(TestDataFactory.createChargersForBatteryTest());
    Charger highPowerKwCharger = TestDataFactory.createDefaultCharger(9999L, 1, 0.0, 0.0);
    highPowerKwCharger.getConnections().get(0).setPowerKW(350L);
    Point foodAdjacentChargerLocation = TestHelperFunctions.findChargerById(chargers, 117934L).getLocation();
    highPowerKwCharger.setLocation(foodAdjacentChargerLocation);
    chargersWithHigherKwAtSameDist.add(highPowerKwCharger);
    TestHelperFunctions.setFoodAdjacentCharger(chargersWithHigherKwAtSameDist, journey, 117934L);


    List<Charger> suitableChargers = null;
    try {
      suitableChargers = routingService.findSuitableChargers(journey, chargersWithHigherKwAtSameDist);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(suitableChargers, 1, 117934L);
    assertTrue(journey.getCurrentBattery() > journey.getMinChargeLevel());
    assertTrue(journey.getCurrentBattery() > journey.getFinalDestinationChargeLevel());
    TestHelperFunctions.assertEqualsWithTolerance(43094, journey.getCurrentBattery(), 100);
  }

  @Test
  @DisplayName("Should not skip chargers")
  public void doesntSkipChargers() {

    List<Charger> inadequateChargers = new ArrayList<>();
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 109188L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 128366L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 72601L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 112482L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 41030L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 90205L));
    inadequateChargers.add(TestHelperFunctions.findChargerById(chargers, 75045L));
    TestHelperFunctions.setFoodAdjacentCharger(inadequateChargers, journey, 72601L);


    for (Charger charger : chargers) {
      System.out.println(charger.getId());
    }

    TestHelperFunctions.setBatteryAndCharging(journey, 0.9, 25000.0, 0.10, 0.9, 0.10);
    List<Charger> suitableChargers = null;

    try {
      suitableChargers = routingService.findSuitableChargers(journey, inadequateChargers);
      TestHelperFunctions.printSuitableChargers(suitableChargers);
    } catch (NoChargerWithinRangeException e) {
      fail("NoChargerWithinRangeException should not have been thrown");
    }

    TestHelperFunctions.assertSuitableChargers(
            suitableChargers, 5, 109188L, 128366L, 72601L, 41030L, 90205L);

  }

}
