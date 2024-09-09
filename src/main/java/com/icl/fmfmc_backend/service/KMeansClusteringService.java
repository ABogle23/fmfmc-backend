package com.icl.fmfmc_backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KMeansClusteringService extends AbstractClusteringService {

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
      return new ArrayList<>(uniquePoints);
    }

    // randomly create centroids
    List<Point> centroids = initializeCentroids(chargers, k);
    List<Point> oldCentroids = null;
    Integer MAX_ITERATIONS = 50;

    // converge centroids
    while (!hasConverged(centroids, oldCentroids) && MAX_ITERATIONS-- > 0) {
      oldCentroids = new ArrayList<>(centroids);

      // assign charger locations to clusters
      List<List<Point>> clusters = assignToClusters(chargers, centroids);

      // recalc centroids
      centroids = recalculateCentroids(clusters);
      System.out.println("calculating centroids");
    }

    return centroids;
  }




  }
