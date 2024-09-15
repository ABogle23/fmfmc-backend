package com.icl.fmfmc_backend.service;

import java.util.*;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
public class KMeansPlusPlusClusteringService extends AbstractClusteringService {

  @Override
  public List<Point> clusterChargers(List<Point> chargers, int k) {

    System.out.println("size of chargers: " + chargers.size());

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
      return new ArrayList<>(uniquePoints);
    }

    // create new list of unique charger locations to avoid modifying the original list
    List<Point> chargerLocations = new ArrayList<>(uniquePoints);

    Integer MAX_ITERATIONS = 50;

    // init centroids with ++
    List<Point> centroids = initializeCentroidsPlusPlus(chargerLocations, k);
    List<Point> oldCentroids = null;
    for (Point charger : centroids) {
      System.out.println(charger.getY() + "," + charger.getX() + ",blue,marker");
    }
    while (!hasConverged(centroids, oldCentroids) && MAX_ITERATIONS-- > 0) {

      oldCentroids = new ArrayList<>(centroids);

      List<List<Point>> clusters = assignToClusters(chargerLocations, centroids);

      int count = 0;
      for (int i = 0; i < clusters.size(); i++) {
        count += clusters.get(i).size();
      }
      System.out.println("Loop start Total cluster chargers: " + count);

      System.out.println("Clustering iteration: " + (50 - MAX_ITERATIONS));

      // iteratively remove outliers from clusters and recalculate centroids
      removeOutliers(clusters, chargerLocations);
      centroids = recalculateCentroidsMedian(clusters);
      System.out.println("calculating centroids");

      for (Point charger : chargerLocations) {
        System.out.println(charger.getY() + "," + charger.getX() + ",red,circle");
      }

      for (Point charger : centroids) {
        System.out.println(charger.getY() + "," + charger.getX() + ",blue,marker");
      }

      count = 0;
      for (int i = 0; i < clusters.size(); i++) {
        count += clusters.get(i).size();
      }
      System.out.println("Total cluster chargers: " + count);
    }

    return centroids;
  }

  private void removeOutliers(List<List<Point>> clusters, List<Point> originalClusterSet) {
    for (List<Point> cluster : clusters) {
      if (cluster.isEmpty()) {
        continue;
      }
      // get median point
      Point centroid = calculateMedianPoint(cluster);
      double avgDistance =
          cluster.stream().mapToDouble(p -> p.distance(centroid)).average().orElse(0);
      double threshold = avgDistance * 1.5; // set threshold tp 150% of avg dist
      //      cluster.removeIf(p -> p.distance(centroid) > threshold); // remove outliers in cluster

      // identify outliers in cluster
      List<Point> outliers =
          cluster.stream().filter(p -> p.distance(centroid) > threshold).toList();

      // remove outliers from cluster
      cluster.removeAll(outliers);

      // remove outliers from originalClusterSet
      originalClusterSet.removeAll(outliers);
    }
  }

  private List<Point> recalculateCentroidsMedian(List<List<Point>> clusters) {
    List<Point> centroids = new ArrayList<>();
    for (List<Point> cluster : clusters) {
      if (cluster.isEmpty()) continue;

      // calc median point for each cluster
      Point medianPoint = calculateMedianPoint(cluster);

      if (medianPoint != null) {
        centroids.add(medianPoint);
      }
    }
    return centroids;
  }

  private Point calculateMedianPoint(List<Point> cluster) {

    Point medianPoint = null;
    double minDistanceSum = Double.MAX_VALUE;

    // calc sum of distances for each point to all others in the cluster
    for (Point point : cluster) {
      double distanceSum = 0;
      for (Point otherPoint : cluster) {
        if (point != otherPoint) {
          distanceSum += point.distance(otherPoint);
        }
      }

      // update the median point if the cur point has smaller distance sum
      if (distanceSum < minDistanceSum) {
        minDistanceSum = distanceSum;
        medianPoint = point;
      }
    }

    return medianPoint;
  }
}
