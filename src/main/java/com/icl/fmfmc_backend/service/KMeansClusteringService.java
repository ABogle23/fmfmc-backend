package com.icl.fmfmc_backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class KMeansClusteringService implements ClusteringStrategy {


    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public List<Point> clusterChargers(List<Point> chargers, int k) {
        // randomly create centroids
        List<Point> centroids = initializeCentroidsPlusPlus(chargers, k);
        List<Point> oldCentroids = null;

        // converge centroids
        while (!centroids.equals(oldCentroids)) {
            oldCentroids = new ArrayList<>(centroids);

            // assign charger locations to clusters
            List<List<Point>> clusters = assignToClusters(chargers, centroids);

            // recal centroids
            centroids = recalculateCentroids(clusters);
            System.out.println("calculating centroids");
        }

        List<List<Point>> finalClusters = assignToClusters(chargers, centroids);
        for (int i = 0; i < finalClusters.size(); i++) {
            System.out.println("Centroid " + (i + 1) + " has " + finalClusters.get(i).size() + " chargers.");
        }

        return centroids;
    }

    private static List<Point> initializeCentroids(List<Point> points, int k) {
        Random random = new Random();
        List<Point> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Point centroid = points.get(random.nextInt(points.size()));
            centroids.add(centroid);
        }
        return centroids;
    }

    private static List<Point> initializeCentroidsPlusPlus(List<Point> points, int k) {
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

    private static List<List<Point>> assignToClusters(List<Point> points, List<Point> centroids) {
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

    private static List<Point> recalculateCentroids(List<List<Point>> clusters) {
        List<Point> centroids = new ArrayList<>();
        for (List<Point> cluster : clusters) {
            double sumX = 0, sumY = 0;
            for (Point point : cluster) {
                sumX += point.getX();
                sumY += point.getY();
            }
            centroids.add(geometryFactory.createPoint(new Coordinate(sumX / cluster.size(), sumY / cluster.size())));
        }
        return centroids;
    }

}