package com.icl.fmfmc_backend;

import static org.junit.jupiter.api.Assertions.*;

import com.icl.fmfmc_backend.service.OutlierAdjustedKMeansClusteringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.*;

public class OutlierAdjustedKMeansClusteringServiceTest {

  private OutlierAdjustedKMeansClusteringService service;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    service = new OutlierAdjustedKMeansClusteringService();
    geometryFactory = new GeometryFactory();
  }

  @Test
  void clusterChargersClustersThreeDistinctBalancedGroupsIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (runBalancedGroupsTest()) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(successCount >= 0.8 * totalRuns, "Success rate below 80%: " + successCount + "/" + totalRuns);
  }

  @Test
  void clusterChargersClustersThreeDistinctUnbalancedGroupsIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (runUnbalancedGroupsTest()) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(successCount >= 0.75 * totalRuns, "Success rate below 80%: " + successCount + "/" + totalRuns);
  }

  @Test
  void clusterChargersClustersThreeDistinctBalancedGroupsIntoTwoClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (runBalancedGroupsTest()) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(successCount >= 0.8 * totalRuns, "Success rate below 80%: " + successCount + "/" + totalRuns);
  }

  private boolean runBalancedGroupsTest() {
    List<List<Point>> groups = new ArrayList<>();
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(1.1, 1.1)),
            geometryFactory.createPoint(new Coordinate(0.9, 0.9))
    ));
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(10, 10)),
            geometryFactory.createPoint(new Coordinate(10.1, 10.1)),
            geometryFactory.createPoint(new Coordinate(9.9, 9.9))
    ));
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(20, 20)),
            geometryFactory.createPoint(new Coordinate(20.1, 20.1)),
            geometryFactory.createPoint(new Coordinate(19.9, 19.9))
    ));

    List<Point> chargers = new ArrayList<>();
    groups.forEach(chargers::addAll);

    List<Point> result = service.clusterChargers(chargers, 3);
    return validateClusters(result, groups);
  }

  private boolean runUnbalancedGroupsTest() {
    List<List<Point>> groups = new ArrayList<>();
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(0.80, 0.80)),
            geometryFactory.createPoint(new Coordinate(0.85, 0.85)),
            geometryFactory.createPoint(new Coordinate(0.9, 0.9)),
            geometryFactory.createPoint(new Coordinate(0.95, 0.95)),
            geometryFactory.createPoint(new Coordinate(1.0, 1.0)),
            geometryFactory.createPoint(new Coordinate(1.05, 1.05)),
            geometryFactory.createPoint(new Coordinate(1.1, 1.1)),
            geometryFactory.createPoint(new Coordinate(1.15, 1.15)),
            geometryFactory.createPoint(new Coordinate(1.2, 1.2))
    ));
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(10, 10)),
            geometryFactory.createPoint(new Coordinate(10.1, 10.1))
    ));
    groups.add(Arrays.asList(
            geometryFactory.createPoint(new Coordinate(20, 20)),
            geometryFactory.createPoint(new Coordinate(20.1, 20.1)),
            geometryFactory.createPoint(new Coordinate(19.9, 19.9))
    ));

    List<Point> chargers = new ArrayList<>();
    groups.forEach(chargers::addAll);

    List<Point> result = service.clusterChargers(chargers, 3);
    return validateClusters(result, groups);
  }

  private boolean validateClusters(List<Point> result, List<List<Point>> groups) {
    if (result.size() != 3) {
      return false;
    }
    for (List<Point> group : groups) {
      Point closestCentroid = result.stream()
              .min(Comparator.comparingDouble(p -> p.distance(group.get(0))))
              .orElse(null);
      if (!group.stream().allMatch(p -> p.distance(closestCentroid) < 1)) {
        return false;
      }
    }
    return true;
  }

  @Test
  void clusterChargersReturnsEmptyListWhenChargersListIsEmpty() {
    List<Point> chargers = Collections.emptyList();
    List<Point> result = service.clusterChargers(chargers, 3);
    assertTrue(result.isEmpty());
  }

  @Test
  void clusterChargersReturnsSinglePointWhenChargersListHasOnePoint() {
    List<Point> chargers = Arrays.asList(geometryFactory.createPoint(new Coordinate(1, 1)));
    List<Point> result = service.clusterChargers(chargers, 3);
    assertEquals(1, result.size());
    assertEquals(chargers.get(0), result.get(0));
  }

  @Test
  void clusterChargersReturnsSameListWhenKIsZero() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)));
    List<Point> result = service.clusterChargers(chargers, 0);
    assertEquals(chargers, result);
  }

  @Test
  void clusterChargersReturnsSameListWhenKIsNegative() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)));
    List<Point> result = service.clusterChargers(chargers, -1);
    assertEquals(chargers, result);
  }

  @Test
  void clusterChargersReturnsCentroidsWhenKIsOne() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)),
            geometryFactory.createPoint(new Coordinate(3, 3)));
    List<Point> result = service.clusterChargers(chargers, 1);
    assertEquals(1, result.size());
  }

  @Test
  void clusterChargersReturnsCentroidsWhenKIsLessThanChargersSize() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)),
            geometryFactory.createPoint(new Coordinate(3, 3)),
            geometryFactory.createPoint(new Coordinate(4, 4)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }

  @Test
  void clusterChargersReturnsSameListWhenKIsEqualToChargersSize() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)),
            geometryFactory.createPoint(new Coordinate(3, 3)));
    List<Point> result = service.clusterChargers(chargers, 3);
    assertEquals(chargers, result);
  }

  @Test
  void clusterChargersRemovesOutliersWhenChargersContainOutliers() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 2)),
            geometryFactory.createPoint(new Coordinate(100, 100)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }

  @Test
  void clusterChargersHandlesLargeNumberOfPoints() {
    List<Point> chargers = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      chargers.add(geometryFactory.createPoint(new Coordinate(i, i)));
    }
    List<Point> result = service.clusterChargers(chargers, 10);
    assertEquals(10, result.size());
  }

  @Test
  void clusterChargersHandlesIdenticalPoints() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(1, 1)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(1, result.size());
  }

  @Test
  void clusterChargersHandlesPointsWithSameXCoordinate() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(1, 2)),
            geometryFactory.createPoint(new Coordinate(1, 3)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }

  @Test
  void clusterChargersHandlesPointsWithSameYCoordinate() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
            geometryFactory.createPoint(new Coordinate(2, 1)),
            geometryFactory.createPoint(new Coordinate(3, 1)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }

  @Test
  void clusterChargersHandlesPointsWithNegativeCoordinates() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(-1, -1)),
            geometryFactory.createPoint(new Coordinate(-2, -2)),
            geometryFactory.createPoint(new Coordinate(-3, -3)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }

  @Test
  void clusterChargersHandlesPointsWithMixedCoordinates() {
    List<Point> chargers =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(-1, -1)),
            geometryFactory.createPoint(new Coordinate(2, 2)),
            geometryFactory.createPoint(new Coordinate(-3, 3)),
            geometryFactory.createPoint(new Coordinate(4, -4)));
    List<Point> result = service.clusterChargers(chargers, 2);
    assertEquals(2, result.size());
  }
}
