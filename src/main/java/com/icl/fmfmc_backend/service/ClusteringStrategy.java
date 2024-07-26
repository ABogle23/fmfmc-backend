package com.icl.fmfmc_backend.service;

import org.locationtech.jts.geom.Point;
import java.util.List;

public interface ClusteringStrategy {
  List<Point> clusterChargers(List<Point> points, int numberOfClusters);
  List<Point> consolidateCloseCentroids(List<Point> centroids, Double threshold);
}
