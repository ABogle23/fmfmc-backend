package com.icl.fmfmc_backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class AbstractClusteringService implements ClusteringStrategy {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  @Override
  public List<Point> clusterChargers(List<Point> points, int numberOfClusters) {
    return null;
  }

  @Override
  public List<Point> consolidateCloseCentroids(List<Point> centroids, Double threshold) {
    // threshold is the maximum distance between two centroids in degrees, 0.01 = 1.11 km
    List<Point> consolidatedCentroids = new ArrayList<>(centroids);
    Boolean isConsolidated = true;
    while (isConsolidated) {
      isConsolidated = false;
      for (int i = 0; i < consolidatedCentroids.size(); i++) {
        for (int j = i + 1; j < consolidatedCentroids.size(); j++) {
          Point pointI = consolidatedCentroids.get(i);
          Point pointJ = consolidatedCentroids.get(j);
          if (pointI.distance(pointJ) < threshold) {
            // avg of two centroids
            Double newX = (pointI.getX() + pointJ.getX()) / 2;
            Double newY = (pointI.getY() + pointJ.getY()) / 2;
            consolidatedCentroids.set(
                i, geometryFactory.createPoint(new Coordinate(newX, newY))); // update centroid
            consolidatedCentroids.remove(j); // drop old centroid
            isConsolidated = true;
            break;
          }
        }
        if (isConsolidated) {
          break;
        }
      }
    }
    return consolidatedCentroids;
  }

  protected List<Point> initializeCentroids(List<Point> points, int k) {
    Random random = new Random();
    List<Point> centroids = new ArrayList<>();
    for (int i = 0; i < k; i++) {
      Point centroid = points.get(random.nextInt(points.size()));
      centroids.add(centroid);
    }
    return centroids;
  }

  // https://stats.stackexchange.com/questions/272114/using-kmeans-computing-weighted-probability-for-kmeans-initialization
  protected List<Point> initializeCentroidsPlusPlus(List<Point> points, int k) {
    List<Point> centroids = new ArrayList<>();
    Random random = new Random();

    // TODO: could try point assigned completely random instead of random provided points then find
    // nearest
    // pick first centroid at random from provided points
    centroids.add(points.get(random.nextInt(points.size())));

    // pick remaining centroids based on distance from existing centroids
    for (int i = 1; i < k; i++) {
      double[] distances = new double[points.size()];
      for (int j = 0; j < points.size(); j++) {
        Point p = points.get(j);
        double minDist = Double.MAX_VALUE;
        for (Point centroid : centroids) {
          // find closest distance to existing centroids
          double dist = p.distance(centroid);
          if (dist < minDist) {
            minDist = dist;
          }
        }
        // convert dist from degrees to meters
        double normalisedMinDist = minDist * 111111.0;
        // square distances
        distances[j] = (normalisedMinDist * normalisedMinDist);
        //        System.out.println("Distance: " + distances[j]);
      }
      // pick new centroid weighted by distance
      double total = Arrays.stream(distances).sum();
      double ran = total * random.nextDouble();
      double sum = 0;
      for (int j = 0; j < distances.length; j++) {
        sum += distances[j];
        if (sum >= ran) {
          // add point when cumulative distance exceeds random value
          centroids.add(points.get(j));
          break;
        }
      }
    }
    return centroids;
  }

  protected List<List<Point>> assignToClusters(List<Point> points, List<Point> centroids) {

    // initialise list for each cluster
    List<List<Point>> clusters = new ArrayList<>();
    for (int i = 0; i < centroids.size(); i++) {
      clusters.add(new ArrayList<>());
    }

    // assign each point to the nearest cluster
    for (Point point : points) {
      int minIndex = -1;
      double minDistance = Double.MAX_VALUE;
      for (int i = 0; i < centroids.size(); i++) {
        double distance = point.distance(centroids.get(i));
        if (distance < minDistance) {
          minDistance = distance;
          minIndex = i;
        }
      }
      clusters.get(minIndex).add(point); // add point to cluster
    }

    return clusters;
  }

  protected boolean hasConverged(List<Point> centroids, List<Point> oldCentroids) {
    if (oldCentroids == null) {
      return false;
    }
    for (int i = 0; i < centroids.size(); i++) {
      if (centroids.get(i).distance(oldCentroids.get(i)) > 0.0009) { // 100m
        return false;
      }
    }
    return true;
  }

  protected List<Point> recalculateCentroids(List<List<Point>> clusters) {
    List<Point> centroids = new ArrayList<>();
    for (List<Point> cluster : clusters) {
      double sumX = 0;
      double sumY = 0;
      for (Point point : cluster) {
        sumX += point.getX();
        sumY += point.getY();
      }
      centroids.add(
          geometryFactory.createPoint(
              new Coordinate(sumX / cluster.size(), sumY / cluster.size())));
    }
    return centroids;
  }
}
