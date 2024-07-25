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
  void testClusterChargersClustersThreeDistinctBalancedGroupsIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctBalancedGroupsIntoThreeClusters(
          Collections.emptyList())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.8 * totalRuns,
        "Success rate below 80%: " + successCount + " out of " + totalRuns);
  }

  @Test
  void testClusterChargersClustersThreeDistinctUnbalancedGroupsIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctUnbalancedGroupsIntoThreeClusters(
          Collections.emptyList())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.75 * totalRuns,
        "Success rate below 80%: " + successCount + " out of " + totalRuns);
  }

  @Test
  void testClusterChargersClustersThreeDistinctUnbalancedGroupsIntoTwoClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctUnbalancedGroupsIntoTwoClusters(
          Collections.emptyList())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.75 * totalRuns,
        "Success rate below 80%: " + successCount + " out of " + totalRuns);
  }

  @Test
  void testClusterChargersClustersThreeDistinctBalancedGroupsWithOutliersIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctBalancedGroupsIntoThreeClusters(getOutliers())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.75 * totalRuns,
        "Success rate below 75%: " + successCount + " out of " + totalRuns);
  }

  @Test
  void testClusterChargersClustersThreeDistinctUnbalancedGroupsWithOutliersIntoThreeClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctUnbalancedGroupsIntoThreeClusters(getOutliers())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.75 * totalRuns,
        "Success rate below 75%: " + successCount + " out of " + totalRuns);
  }

  @Test
  void testClusterChargersClustersThreeDistinctUnbalancedGroupsWithOutliersIntoTwoClusters() {
    int successCount = 0;
    int totalRuns = 100;
    for (int i = 0; i < totalRuns; i++) {
      if (clusterChargersClustersThreeDistinctUnbalancedGroupsIntoTwoClusters(getOutliers())) {
        successCount++;
      }
    }
    System.out.println("Success rate: " + successCount + "/" + totalRuns);
    assertTrue(
        successCount >= 0.75 * totalRuns,
        "Success rate below 75%: " + successCount + " out of " + totalRuns);
  }

  private boolean clusterChargersClustersThreeDistinctBalancedGroupsIntoThreeClusters(
      List<Point> outliers) {
    List<Point> group1 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(1, 1)),
                geometryFactory.createPoint(new Coordinate(1.1, 1.1)),
                geometryFactory.createPoint(new Coordinate(1.2, 1.2)),
            geometryFactory.createPoint(new Coordinate(0.9, 0.9)));
    List<Point> group2 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(10, 10)),
                geometryFactory.createPoint(new Coordinate(10.1, 10.1)),
                geometryFactory.createPoint(new Coordinate(10.2, 10.2)),
            geometryFactory.createPoint(new Coordinate(9.9, 9.9)));
    List<Point> group3 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(20, 20)),
            geometryFactory.createPoint(new Coordinate(20.1, 20.1)),
                geometryFactory.createPoint(new Coordinate(20.2, 20.2)),
                geometryFactory.createPoint(new Coordinate(19.9, 19.9)));

    List<Point> chargers = new ArrayList<>();
    chargers.addAll(group1);
    chargers.addAll(group2);
    chargers.addAll(group3);
    chargers.addAll(outliers);

    List<Point> result = service.clusterChargers(chargers, 3);
    boolean valid = result.size() == 3;
    System.out.println(result);

    if (valid) {
//      for (Point point : group1) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group1.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }
//      for (Point point : group2) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group2.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }
//      for (Point point : group3) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group3.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }

      int validCount = 0;

      if (outliers.isEmpty()) {
        valid &= allPointsCloseToCentroid(group1, result);
        valid &= allPointsCloseToCentroid(group2, result);
        valid &= allPointsCloseToCentroid(group3, result);
        return valid;
      } else {
        if (checkProximity(group1, result, 0.75)) {validCount++;}
        if (checkProximity(group2, result, 0.75)) {validCount++;}
        if (checkProximity(group3, result, 0.75)) {validCount++;}
        return validCount >= 2;
      }
    }

    return valid;
  }

  private boolean clusterChargersClustersThreeDistinctUnbalancedGroupsIntoThreeClusters(
      List<Point> outliers) {
    List<Point> group1 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(0.80, 0.80)),
            geometryFactory.createPoint(new Coordinate(0.85, 0.85)),
            geometryFactory.createPoint(new Coordinate(0.9, 0.9)),
            geometryFactory.createPoint(new Coordinate(0.95, 0.95)),
            geometryFactory.createPoint(new Coordinate(0.99, 0.99)),
            geometryFactory.createPoint(new Coordinate(1.0, 1.0)),
            geometryFactory.createPoint(new Coordinate(1.01, 1.01)),
            geometryFactory.createPoint(new Coordinate(1.03, 1.03)),
            geometryFactory.createPoint(new Coordinate(1.04, 1.04)),
            geometryFactory.createPoint(new Coordinate(1.05, 1.05)),
            geometryFactory.createPoint(new Coordinate(1.1, 1.1)),
            geometryFactory.createPoint(new Coordinate(1.15, 1.15)),
            geometryFactory.createPoint(new Coordinate(1.2, 1.2)));
    List<Point> group2 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(9.9, 9.9)),
            geometryFactory.createPoint(new Coordinate(10, 10)),
            geometryFactory.createPoint(new Coordinate(10.05, 10.05)),
            geometryFactory.createPoint(new Coordinate(10.1, 10.1)));
    List<Point> group3 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(20, 20)),
            geometryFactory.createPoint(new Coordinate(20.05, 20.05)),
            geometryFactory.createPoint(new Coordinate(20.1, 20.1)),
            geometryFactory.createPoint(new Coordinate(19.9, 19.9)));

    List<Point> chargers = new ArrayList<>();
    chargers.addAll(group1);
    chargers.addAll(group2);
    chargers.addAll(group3);
    chargers.addAll(outliers);

    List<Point> result = service.clusterChargers(chargers, 3);
    boolean valid = result.size() == 3;
    System.out.println(result);

    if (valid) {
//      for (Point point : group1) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group1.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }
//      for (Point point : group2) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group2.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }
//      for (Point point : group3) {
//        Point closestCentroid =
//            result.stream().min(Comparator.comparingDouble(point::distance)).orElse(null);
//        valid &= group3.stream().allMatch(p -> p.distance(closestCentroid) < 1);
//      }
      if (outliers.isEmpty()) {
        valid &= allPointsCloseToCentroid(group1, result);
        valid &= allPointsCloseToCentroid(group2, result) || allPointsCloseToCentroid(group3, result);
      } else {
        valid &= checkProximity(group1, result, 0.75);
        valid &= checkProximity(group2, result, 0.75) || checkProximity(group3, result, 0.75);
      }
    }

    return valid;
  }

  private boolean clusterChargersClustersThreeDistinctUnbalancedGroupsIntoTwoClusters(
      List<Point> outliers) {
    List<Point> group1 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(0.80, 0.80)),
            geometryFactory.createPoint(new Coordinate(0.85, 0.85)),
            geometryFactory.createPoint(new Coordinate(0.9, 0.9)),
            geometryFactory.createPoint(new Coordinate(0.95, 0.95)),
            geometryFactory.createPoint(new Coordinate(0.99, 0.99)),
            geometryFactory.createPoint(new Coordinate(1.0, 1.0)),
            geometryFactory.createPoint(new Coordinate(1.01, 1.01)),
            geometryFactory.createPoint(new Coordinate(1.03, 1.03)),
            geometryFactory.createPoint(new Coordinate(1.04, 1.04)),
            geometryFactory.createPoint(new Coordinate(1.05, 1.05)),
            geometryFactory.createPoint(new Coordinate(1.1, 1.1)),
            geometryFactory.createPoint(new Coordinate(1.15, 1.15)),
            geometryFactory.createPoint(new Coordinate(1.2, 1.2)));
    List<Point> group2 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(9.9, 9.9)),
            geometryFactory.createPoint(new Coordinate(10, 10)),
            geometryFactory.createPoint(new Coordinate(10.05, 10.05)),
            geometryFactory.createPoint(new Coordinate(10.1, 10.1)));
    List<Point> group3 =
        Arrays.asList(
            geometryFactory.createPoint(new Coordinate(20, 20)),
            geometryFactory.createPoint(new Coordinate(20.05, 20.05)),
            geometryFactory.createPoint(new Coordinate(20.1, 20.1)),
            geometryFactory.createPoint(new Coordinate(19.9, 19.9)));

    List<Point> chargers = new ArrayList<>();
    chargers.addAll(group1);
    chargers.addAll(group2);
    chargers.addAll(group3);
    chargers.addAll(outliers);

    List<Point> result = service.clusterChargers(chargers, 2);
    boolean valid = result.size() == 2;
    System.out.println(result);

    if (valid) {
      if (outliers.isEmpty()) {
        valid &= allPointsCloseToCentroid(group1, result);
        valid &= allPointsCloseToCentroid(group2, result) || allPointsCloseToCentroid(group3, result);
      } else {
        valid &= checkProximity(group1, result, 0.75);
        valid &= checkProximity(group2, result, 0.75) || checkProximity(group3, result, 0.75);
      }
    }

    return valid;
  }

  private boolean allPointsCloseToCentroid(List<Point> group, List<Point> centroids) {
    Point closestCentroid =
        centroids.stream()
            .min(Comparator.comparingDouble(p -> p.distance(group.get(0))))
            .orElse(null);
    return group.stream().allMatch(p -> p.distance(closestCentroid) < 1);
  }

  private boolean checkProximity(List<Point> group, List<Point> centroids, double proximityRatio) {
    int countClosePoints = 0;
    for (Point point : group) {
      Point closestCentroid =
          centroids.stream().min(Comparator.comparingDouble(p -> p.distance(point))).orElse(null);
      if (closestCentroid != null && point.distance(closestCentroid) < 1) {
        countClosePoints++;
      }
    }
    System.out.println("Close points: " + countClosePoints + "/" + group.size());
    return ((double) countClosePoints / group.size()) >= proximityRatio;
  }

  private List<Point> getOutliers() {
    return Arrays.asList(
        geometryFactory.createPoint(new Coordinate(60.0, 60.0)),
        //        geometryFactory.createPoint(new Coordinate(40.0, 40.0)),
        //        geometryFactory.createPoint(new Coordinate(-10.0, -10.0)),
        //        geometryFactory.createPoint(new Coordinate(-20.0, -20.0)),
        //        geometryFactory.createPoint(new Coordinate(-30.0, -30.0)),
        geometryFactory.createPoint(new Coordinate(-40.0, -40.0)));
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
