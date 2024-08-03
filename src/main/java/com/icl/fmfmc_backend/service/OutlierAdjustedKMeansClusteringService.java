package com.icl.fmfmc_backend.service;

import java.util.*;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
public class OutlierAdjustedKMeansClusteringService extends AbstractClusteringService {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  @Override
  public List<Point> clusterChargers(List<Point> chargers, int k) {

    // set default number of clusters to 4 if below 0
    if (k <= 0) {
      k = 4;
    }

    // prevents clustering if there are fewer unique chargers than clusters
    Set<Point> uniquePoints = new HashSet<>(chargers);
    if (uniquePoints.size() < k) {
      k = uniquePoints.size();
      System.out.println("Number of clusters set to " + k);
    }

    // prevents clustering if there are fewer chargers than clusters
    if (k >= uniquePoints.size()) {
      System.out.println("Number of clusters set to " + k);
      return uniquePoints.stream().collect(Collectors.toList());
    }

    Integer MAX_ITERATIONS = 200;

    // outlier filtering before clustering
    chargers = filterOutliers(chargers);
    System.out.println("Filtering outliers by Z-Score: " + chargers);

    List<Point> centroids = initializeCentroidsPlusPlus(chargers, k);
    List<Point> oldCentroids = null;

    while (!centroids.equals(oldCentroids) && MAX_ITERATIONS-- > 0) {
      oldCentroids = new ArrayList<>(centroids);

      List<List<Point>> clusters = assignToClusters(chargers, centroids);
      // iteratively remove outliers from clusters
      refineClusters(clusters);
      centroids = recalculateCentroids(clusters);
      //      System.out.println("calculating centroids");
    }

    return centroids;
  }

  private static List<Point> recalculateCentroids(List<List<Point>> clusters) {
    List<Point> centroids = new ArrayList<>();
    for (List<Point> cluster : clusters) {
      double sumX = 0, sumY = 0, totalWeight = 0;
      for (Point point : cluster) {
        double weight = 1 / (point.distance(cluster.get(0)) + 0.0001); // avoid division by zero
        sumX += point.getX() * weight;
        sumY += point.getY() * weight;
        totalWeight += weight;
      }
      centroids.add(
          geometryFactory.createPoint(new Coordinate(sumX / totalWeight, sumY / totalWeight)));
    }
    return centroids;
  }

  private static List<Point> filterOutliers(List<Point> points) {
    System.out.println("Filtering outliers by Z-Score: " + points);
    double thresholdDistance = calculateThresholdDistance(points);
    Point medianPoint = calculateMedianPoint(points);
    return points.stream()
        .filter(p -> p.distance(medianPoint) <= thresholdDistance)
        .collect(Collectors.toList());
  }

  private static double calculateThresholdDistance(List<Point> points) {
    List<Double> distances =
        points.stream()
            .map(p -> p.distance(calculateMedianPoint(points)))
            .sorted()
            .collect(Collectors.toList());
    int q1Index = distances.size() / 4;
    int q3Index = (int) (distances.size() * 0.75);
    double iqr = distances.get(q3Index) - distances.get(q1Index);
    return distances.get(q3Index) + 1.5 * iqr;
  }

  private static Point calculateMedianPoint(List<Point> points) {
    int medianIndex = points.size() / 2;
    return points.get(medianIndex);
  }


  @Deprecated
  private static List<Point> filterOutliersByZScore(List<Point> points) {

    System.out.println("Filtering outliers by Z-Score: " + points);

    double mean =
        points.stream()
            .mapToDouble(p -> p.distance(geometryFactory.createPoint(new Coordinate(0, 0))))
            .average()
            .orElse(0);
    double stdev =
        Math.sqrt(
            points.stream()
                .mapToDouble(
                    p ->
                        Math.pow(
                            p.distance(geometryFactory.createPoint(new Coordinate(0, 0))) - mean,
                            2))
                .average()
                .orElse(0));

    return points.stream()
        .filter(
            p ->
                Math.abs(p.distance(geometryFactory.createPoint(new Coordinate(0, 0))) - mean)
                        / stdev
                    < 3)
        .collect(Collectors.toList());
  }

  private static void refineClusters(List<List<Point>> clusters) {
    for (List<Point> cluster : clusters) {
      if (cluster.isEmpty()) continue;

      Point centroid = calculateCentroid(cluster);
      double avgDistance =
          cluster.stream().mapToDouble(p -> p.distance(centroid)).average().orElse(0);
      double threshold = avgDistance * 1.5; // set threshold tp 150% of avg dist
      cluster.removeIf(p -> p.distance(centroid) > threshold); // remove outliers in cluster
    }
  }

  private static Point calculateCentroid(List<Point> cluster) {
    double sumX = 0, sumY = 0;
    for (Point point : cluster) {
      sumX += point.getX();
      sumY += point.getY();
    }
    return geometryFactory.createPoint(
        new Coordinate(sumX / cluster.size(), sumY / cluster.size()));
  }

}
