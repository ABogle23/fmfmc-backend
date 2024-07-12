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


}