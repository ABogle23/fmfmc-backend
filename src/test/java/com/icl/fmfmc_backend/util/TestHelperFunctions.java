package com.icl.fmfmc_backend.util;

import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.routing.Journey;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            Journey journey,
            Double currentBattery,
            Double evRange,
            Double minChargeLevel,
            Double chargerLevelAfterEachStop,
            Double finalDestinationChargeLevel) {
      journey.setCurrentBattery(currentBattery * evRange);
      journey.setEvRange(evRange);
      journey.setMinChargeLevelPct(minChargeLevel);
      journey.setMinChargeLevel(journey.getEvRange() * journey.getMinChargeLevelPct());
      journey.setChargeLevelAfterEachStopPct(chargerLevelAfterEachStop);
      journey.setChargeLevelAfterEachStop(journey.getEvRange() * journey.getChargeLevelAfterEachStopPct());
      journey.setFinalDestinationChargeLevelPct(finalDestinationChargeLevel);
      journey.setFinalDestinationChargeLevel(
          journey.getEvRange() * journey.getFinalDestinationChargeLevelPct());
    }

    private static void setBatteryAndChargingToRelaxed(Journey journey) {
      Double minChargeLevel = 0.1;
      Double chargerLevelAfterEachStop = 1.0;
      Double finalDestinationChargeLevel = 0.2;
      journey.setMinChargeLevelPct(minChargeLevel);
      journey.setMinChargeLevel(journey.getEvRange() * journey.getMinChargeLevelPct());
      journey.setChargeLevelAfterEachStopPct(chargerLevelAfterEachStop);
      journey.setChargeLevelAfterEachStop(journey.getEvRange() * journey.getChargeLevelAfterEachStopPct());
      journey.setFinalDestinationChargeLevelPct(finalDestinationChargeLevel);
      journey.setFinalDestinationChargeLevel(
          journey.getEvRange() * journey.getFinalDestinationChargeLevelPct());
    }

    public static void setFoodAdjacentCharger(List<Charger> chargers, Journey journey, Long chargerId) {

        journey.setStopForEating(true);

        Optional<Charger> foodAdjacentCharger = chargers.stream()
                .filter(charger -> Objects.equals(charger.getId(), chargerId))
                .findFirst();

        foodAdjacentCharger.ifPresent(charger -> journey.setFoodAdjacentCharger(charger));
    }

    public static Charger findChargerById(List<Charger> chargers, Long chargerId) {
        Charger foundCharger = null;
        for (Charger charger : chargers) {
            if (Objects.equals(charger.getId(), chargerId)) {
                foundCharger = charger;
                break;
            }
        }
        return foundCharger;
    }

}
