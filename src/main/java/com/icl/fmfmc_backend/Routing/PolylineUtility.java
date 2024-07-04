package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolylineUtility {

  private static final double PRECISION = 1E5;

  public static String encodeGeoCoordinatesToPolyline(List<GeoCoordinates> geoCoordinatesList) {
    List<List<Double>> coordinates = geoCoordinatesList.stream()
            .map(gc -> List.of(gc.getLongitude(), gc.getLatitude()))
            .collect(Collectors.toList());
    return encodePolyline(coordinates);
  }

  public static String encodePolyline(List<List<Double>> coordinates) {
    StringBuilder encodedPath = new StringBuilder();
    int prevLat = 0, prevLng = 0;

    for (List<Double> coordinate : coordinates) {
      int lat = (int) Math.round(coordinate.get(1) * PRECISION);
      int lng = (int) Math.round(coordinate.get(0) * PRECISION);

      int dLat = lat - prevLat;
      int dLng = lng - prevLng;

      encodedPath.append(encode(dLat));
      encodedPath.append(encode(dLng));

      prevLat = lat;
      prevLng = lng;
    }

    return encodedPath.toString();
  }


  public static String encodeLineString(LineString lineString) {
    List<List<Double>> coordinates = Arrays.stream(lineString.getCoordinates())
            .map(coord -> List.of(coord.x, coord.y))
            .collect(Collectors.toList());
    return encodePolyline(coordinates);
  }

  public static String encodePolygon(Polygon polygon) {
    LineString exteriorRing = polygon.getExteriorRing();
    return encodeLineString(exteriorRing);
  }

  private static String encode(int value) {
    StringBuilder encoded = new StringBuilder();
    value = value < 0 ? ~(value << 1) : value << 1;
    while (value >= 0x20) {
      encoded.append(Character.toChars((0x20 | (value & 0x1f)) + 63));
      value >>= 5;
    }
    encoded.append(Character.toChars(value + 63));
    return encoded.toString();
  }
}
