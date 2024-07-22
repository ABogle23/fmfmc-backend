package com.icl.fmfmc_backend.util;

import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Routing.Route;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestHelperFunctions {
    public static void assertEqualsWithTolerance(
        double expected, double actual, double tolerance, String message) {
      assertTrue(Math.abs(expected - actual) <= tolerance, message);
    }

    public static void assertEqualsWithTolerance(double expected, double actual, double tolerance) {
      assertEqualsWithTolerance(
          expected,
          actual,
          tolerance,
          "Actual value "
              + actual
              + " is not within the expected tolerance of "
              + tolerance
              + " from the expected value "
              + expected);
    }

    public static void printSuitableChargers(List<Charger> suitableChargers) {
      for (Charger charger : suitableChargers) {
        System.out.println(charger.getId());
      }
    }

    public static void assertSuitableChargers(
            List<Charger> suitableChargers, Integer expectedChargerCount, Long... expectedChargerIds) {
      assertNotNull(suitableChargers, "Suitable chargers should not be null");
      assertEquals(
          expectedChargerCount,
          suitableChargers.size(),
          "The no. of suitable chargers found "
              + suitableChargers.size()
              + " does not match the expected count "
              + expectedChargerCount);

      for (int i = 0; i < expectedChargerIds.length; i++) {
        assertEquals(
            expectedChargerIds[i],
            suitableChargers.get(i).getId(),
            "Charger ID not found at index " + i);
      }
    }

    public static void setBatteryAndCharging(
            Route route,
            Double currentBattery,
            Double evRange,
            Double minChargeLevel,
            Double chargerLevelAfterEachStop,
            Double finalDestinationChargeLevel) {
      route.setCurrentBattery(currentBattery * evRange);
      route.setEvRange(evRange);
      route.setMinChargeLevelPct(minChargeLevel);
      route.setMinChargeLevel(route.getEvRange() * route.getMinChargeLevelPct());
      route.setChargeLevelAfterEachStopPct(chargerLevelAfterEachStop);
      route.setChargeLevelAfterEachStop(route.getEvRange() * route.getChargeLevelAfterEachStopPct());
      route.setFinalDestinationChargeLevelPct(finalDestinationChargeLevel);
      route.setFinalDestinationChargeLevel(
          route.getEvRange() * route.getFinalDestinationChargeLevelPct());
    }

    private static void setBatteryAndChargingToRelaxed(Route route) {
      Double minChargeLevel = 0.1;
      Double chargerLevelAfterEachStop = 1.0;
      Double finalDestinationChargeLevel = 0.2;
      route.setMinChargeLevelPct(minChargeLevel);
      route.setMinChargeLevel(route.getEvRange() * route.getMinChargeLevelPct());
      route.setChargeLevelAfterEachStopPct(chargerLevelAfterEachStop);
      route.setChargeLevelAfterEachStop(route.getEvRange() * route.getChargeLevelAfterEachStopPct());
      route.setFinalDestinationChargeLevelPct(finalDestinationChargeLevel);
      route.setFinalDestinationChargeLevel(
          route.getEvRange() * route.getFinalDestinationChargeLevelPct());
    }
}
