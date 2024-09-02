package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import org.locationtech.jts.linearref.LengthIndexedLine;

@Service
public class GeometryService {
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private static final GeometricShapeFactory shapeFactory = new GeometricShapeFactory();

    @Deprecated
    public static Polygon bufferLineStringSquare(LineString lineString, double distance) {
        BufferParameters bufferParameters = new BufferParameters();
        bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
        return (Polygon) BufferOp.bufferOp(lineString, distance, bufferParameters);
    }
    public static Polygon bufferLineString(LineString lineString, double distance) {
        Geometry buffered = lineString.buffer(distance);

        if (buffered instanceof Polygon) {
            return (Polygon) buffered;
        } else if (buffered instanceof MultiPolygon) {
            // Handle MultiPolygon case
            MultiPolygon multiPolygon = (MultiPolygon) buffered;

            System.out.println("Multipolygon: " + multiPolygon.toString());
            // Example: Return the largest polygon or merge them
            // Here, we just throw an exception for simplicity
            return convertMultiPolygonToPolygon(multiPolygon);

//            throw new IllegalArgumentException("Buffer operation resulted in a MultiPolygon");
        } else {
            throw new IllegalArgumentException("Buffer operation resulted in an unexpected geometry type");
        }
    }

    public static Polygon convertMultiPolygonToPolygon(MultiPolygon multiPolygon) {

        List<Polygon> polygons = PolygonExtracter.getPolygons(multiPolygon);

        // Merge all polygons using CascadedPolygonUnion
        Geometry mergedGeometry = CascadedPolygonUnion.union(polygons);

        if (mergedGeometry instanceof Polygon) {
            return (Polygon) mergedGeometry;
        } else {
            throw new IllegalArgumentException("Merged geometry is not a Polygon");
        }
    }

    // buffer a polyline represented as a string
    @Deprecated
    public Polygon bufferPolyline(String polyline, double width) {
        // convert the string to coordinates and then to a LineString
        LineString lineString = createLineStringFromText(polyline);
        System.out.println("Buffering polyline: " + lineString.toString());
        return (Polygon) lineString.buffer(width);
    }

    // buffer a polyline represented as coordinates
    @Deprecated
    public Polygon bufferPolyline(Coordinate[] coordinates, double widthInKm) {
        LineString lineString = geometryFactory.createLineString(coordinates);
        return (Polygon) lineString.buffer(widthInKm);
    }

    // parse a polyline string to a LineString
    @Deprecated
    public LineString createLineStringFromText(String polyline) {
        String[] coords = polyline.split(";");
        Coordinate[] points = Arrays.stream(coords)
                .map(coord -> {
                    String[] xy = coord.split(",");
                    return new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                })
                .toArray(Coordinate[]::new);
        return geometryFactory.createLineString(points);
    }

//    public static LineString createLineStringFromGeoCoordinates(Coordinate[] coordinates) {
//        LineString lineString = geometryFactory.createLineString(coordinates);
//        return lineString;
//    }

    public LineString createLineString(List<GeoCoordinates> geoCoordinatesList) {
        Coordinate[] jtsCoordinates = geoCoordinatesList.stream()
                .map(this::convertToJtsCoordinate)
                .toArray(Coordinate[]::new);
        return geometryFactory.createLineString(jtsCoordinates);
    }

    private Coordinate convertToJtsCoordinate(GeoCoordinates geoCoordinate) {
        return new Coordinate(geoCoordinate.getLongitude(), geoCoordinate.getLatitude());
    }



    public static Double calculateLineStringLength(LineString route) {
        Double totalLength = 0.0;
        Point lastPoint = (Point) route.getStartPoint();

        for (int i = 1; i < route.getNumPoints(); i++) {
            Point currentPoint = route.getPointN(i);
            GeodesicData geodesicData = Geodesic.WGS84.Inverse(
                    lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX());
            Double segmentLength = geodesicData.s12; // Geodesic distance in meters

            totalLength += segmentLength;
            lastPoint = currentPoint;
        }
        return totalLength;
    }


    public static LineString extractLineStringPortion(LineString lineString, Double startFraction, Double endFraction) {
        LengthIndexedLine indexedLine = new LengthIndexedLine(lineString);
        double totalLength = indexedLine.getEndIndex();
        LineString subLine = (LineString) indexedLine.extractLine(startFraction * totalLength, endFraction * totalLength);
        return subLine;
    }

    public static double calculateDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        GeodesicData geodesicData = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
        return geodesicData.s12;  // dist in meters
    }

    public static double calculateDistanceBetweenPoints(Point pointA, Point pointB) {
        double lat1 = pointA.getY();
        double lon1 = pointA.getX();
        double lat2 = pointB.getY();
        double lon2 = pointB.getX();

        GeodesicData geodesicData = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
        return geodesicData.s12;  // dist in meters
    }


    public static Coordinate findClosestCoordinateOnLine(LineString lineString, Point chargerLocation) {

        Double minDistance = Double.MAX_VALUE;
        Coordinate closestCoordinate = null;

        for (Coordinate coordinate : lineString.getCoordinates()) {

            Double distance = GeometryService.calculateDistanceBetweenPoints(
                    chargerLocation.getY(), chargerLocation.getX(), coordinate.y, coordinate.x); // dist in meters

            if (distance < minDistance) {
                minDistance = distance;
                closestCoordinate = coordinate;
            }
        }
//    logger.info("Closest coordinate found: " + closestCoordinate + " with distance: " + minDistance);

        return closestCoordinate;
    }

    public static Double calculateDistanceAlongRouteLineStringToNearestPoint(
            LineString route, Point chargerLocation) {
        Coordinate[] nearestCoordinatesOnRoute = DistanceOp.nearestPoints(route, chargerLocation);
        Point nearestPointOnRoute = new GeometryFactory().createPoint(nearestCoordinatesOnRoute[0]);
        nearestPointOnRoute =
                new GeometryFactory().createPoint(GeometryService.findClosestCoordinateOnLine(route, chargerLocation));
//    logger.info("==========Next Charger==========");
//    logger.info(
//        "Nearest point on route: " + nearestPointOnRoute + " to charger: " + chargerLocation);

        Double cumulativeDistance = 0.0;
        Point lastPoint = (Point) route.getStartPoint();

        for (int i = 0; i < route.getNumPoints(); i++) {
            Point currentPoint = route.getPointN(i);

            Double segmentDistance = GeometryService.calculateDistanceBetweenPoints(
                    lastPoint.getY(), lastPoint.getX(), currentPoint.getY(), currentPoint.getX()); // dist in meters
            //      if (i % 100 == 0) {
            //        logger.info("Cumulative distance - pt " + i + " : " + cumulativeDistance);
            //      }
            if (currentPoint.equalsExact(nearestPointOnRoute, 0.00001)
                    || i == route.getNumPoints() - 1) {
                cumulativeDistance += segmentDistance;
                break;
            }

            cumulativeDistance += segmentDistance;
            lastPoint = currentPoint;
        }

//    logger.info("Cumulative distance: " + cumulativeDistance);
//    logger.info("lastPoint: " + lastPoint);
        return cumulativeDistance;
    }


    public static Double[] getPointAsDouble(Point point) {
        return new Double[]{point.getX(), point.getY()};
    }


    public static MinimumBoundingCircle createMinimumBoundingCircle(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Coordinate[] coordinates = points.stream()
                .map(Point::getCoordinate)
                .toArray(Coordinate[]::new);

        MinimumBoundingCircle minimumBoundingCircle = new MinimumBoundingCircle(geometryFactory.createMultiPointFromCoords(coordinates));
        return minimumBoundingCircle;
    }

    private static Polygon getMinimumBoundingCircleAsPolygon(MinimumBoundingCircle mbc) {
        return (Polygon) mbc.getCircle();
    }

    public static Polygon getMinimumBoundingCircleAsPolygon(MinimumBoundingCircle mbc, Double bufferDistance) {
        Geometry circle = mbc.getCircle();
        return (Polygon) circle.buffer(bufferDistance);
    }

    public static Polygon getBufferedMinimumBoundingCircleAsPolygon(List<Point> points, Double bufferDistance) {
        MinimumBoundingCircle mbc = createMinimumBoundingCircle(points);
        return getMinimumBoundingCircleAsPolygon(mbc, bufferDistance);
    }

    public static Polygon createCircle(Point center, double radiusInMeters) {
        Double radiusInDegrees = radiusInMeters / 111320.0; // 1 deg is approx 111.32 km
        shapeFactory.setCentre(center.getCoordinate());
        shapeFactory.setSize(radiusInDegrees * 2); // diameter
        return shapeFactory.createCircle();
    }

}