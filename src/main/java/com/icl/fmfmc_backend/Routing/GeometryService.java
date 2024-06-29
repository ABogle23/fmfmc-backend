package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;

@Service
public class GeometryService {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    // Function to buffer a polyline represented as a string
    public Polygon bufferPolyline(String polyline, double width) {
        // Convert the string to coordinates and then to a LineString
        LineString lineString = createLineStringFromText(polyline);
        return (Polygon) lineString.buffer(width);
    }

    // Function to buffer a polyline represented as coordinates
    public Polygon bufferPolyline(Coordinate[] coordinates, double widthInKm) {
        LineString lineString = geometryFactory.createLineString(coordinates);
        return (Polygon) lineString.buffer(widthInKm);
    }

    // Helper method to parse a polyline string to a LineString
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

}