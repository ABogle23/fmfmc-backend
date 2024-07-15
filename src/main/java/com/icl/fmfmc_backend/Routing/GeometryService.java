package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import org.locationtech.jts.linearref.LengthIndexedLine;

@Service
public class GeometryService {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public static Polygon bufferLineStringSquare(LineString lineString, double distance) {
        BufferParameters bufferParameters = new BufferParameters();
        bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
        return (Polygon) BufferOp.bufferOp(lineString, distance, bufferParameters);
    }
    public static Polygon bufferLineString(LineString lineString, double distance) {
        return (Polygon) lineString.buffer(distance);
    }









    // buffer a polyline represented as a string
    public Polygon bufferPolyline(String polyline, double width) {
        // Convert the string to coordinates and then to a LineString
        LineString lineString = createLineStringFromText(polyline);
        System.out.println("Buffering polyline: " + lineString.toString());
        return (Polygon) lineString.buffer(width);
    }

    // buffer a polyline represented as coordinates
    public Polygon bufferPolyline(Coordinate[] coordinates, double widthInKm) {
        LineString lineString = geometryFactory.createLineString(coordinates);
        return (Polygon) lineString.buffer(widthInKm);
    }

    // parse a polyline string to a LineString
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

}