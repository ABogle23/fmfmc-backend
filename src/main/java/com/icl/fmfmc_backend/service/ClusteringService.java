package com.icl.fmfmc_backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class ClusteringService {


    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static List<Point> clusterChargers(List<Point> chargers, int k) {
        // randomly create centroids
        List<Point> centroids = initializeCentroids(chargers, k);
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
