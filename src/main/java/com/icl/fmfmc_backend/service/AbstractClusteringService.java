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
    Boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < consolidatedCentroids.size(); i++) {
        for (int j = i + 1; j < consolidatedCentroids.size(); j++) {
          Point ci = consolidatedCentroids.get(i);
          Point cj = consolidatedCentroids.get(j);
          if (ci.distance(cj) < threshold) {
            // avg of two centroids
            Double newX = (ci.getX() + cj.getX()) / 2;
            Double newY = (ci.getY() + cj.getY()) / 2;
            consolidatedCentroids.set(
                i, geometryFactory.createPoint(new Coordinate(newX, newY))); // update centroid
            consolidatedCentroids.remove(j); // drop old centroid
            changed = true;
            break;
          }
        }
        if (changed) {
          break;
        }
      }
    }
    return consolidatedCentroids;
  }

  protected static List<Point> initializeCentroidsPlusPlus(List<Point> points, int k) {
    List<Point> centroids = new ArrayList<>();
    Random random = new Random();
    // pick first centroid at random
    centroids.add(points.get(random.nextInt(points.size())));

    for (int i = 1; i < k; i++) {
      double[] distances = new double[points.size()];
      for (int j = 0; j < points.size(); j++) {
        Point p = points.get(j);
        double minDist = Double.MAX_VALUE;
        for (Point centroid : centroids) {
          double dist = p.distance(centroid);
          if (dist < minDist) {
            minDist = dist;
          }
        }
        distances[j] = minDist;
      }
      // pick new centroid weighted by distance
      double total = Arrays.stream(distances).sum();
      double r = total * random.nextDouble();
      double sum = 0;
      for (int j = 0; j < distances.length; j++) {
        sum += distances[j];
        if (sum >= r) {
          centroids.add(points.get(j));
          break;
        }
      }
    }
    return centroids;
  }

  protected static List<List<Point>> assignToClusters(List<Point> points, List<Point> centroids) {
    List<List<Point>> clusters = new ArrayList<>();
    for (int i = 0; i < centroids.size(); i++) {
      clusters.add(new ArrayList<>());
    }

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
      clusters.get(minIndex).add(point);
    }

    return clusters;
  }
}
