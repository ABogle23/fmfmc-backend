package com.icl.fmfmc_backend.util;

import com.icl.fmfmc_backend.entity.Charger.Charger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
