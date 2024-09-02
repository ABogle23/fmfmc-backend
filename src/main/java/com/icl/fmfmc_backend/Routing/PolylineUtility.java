package com.icl.fmfmc_backend.Routing;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.service.CoordinateFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolylineUtility {

  private static final double PRECISION = 1E5;

  /**
   * Encodes a list of GeoCoordinates to a polyline string.
   *
   * @param geoCoordinatesList the list of GeoCoordinates to encode
   * @return the encoded polyline string
   */
  public static String encodeGeoCoordinatesToPolyline(List<GeoCoordinates> geoCoordinatesList) {
    List<List<Double>> coordinates = geoCoordinatesList.stream()
            .map(gc -> List.of(gc.getLongitude(), gc.getLatitude()))
            .collect(Collectors.toList());
    return encodePolyline(coordinates);
  }
  /**
   * Encodes a list of coordinates to a polyline string.
   *
   * @param coordinates the list of coordinates to encode
   * @return the encoded polyline string
   */
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

  /**
   * Encodes a LineString to a polyline string.
   *
   * @param lineString the LineString to encode
   * @return the encoded polyline string
   */
  public static String encodeLineString(LineString lineString) {
    List<List<Double>> coordinates = Arrays.stream(lineString.getCoordinates())
            .map(coord -> List.of(coord.x, coord.y))
            .collect(Collectors.toList());
    return encodePolyline(coordinates);
  }
  /**
   * Encodes a Polygon to a polyline string.
   *
   * @param polygon the Polygon to encode
   * @return the encoded polyline string
   */
  public static String encodePolygon(Polygon polygon) {
    LineString exteriorRing = polygon.getExteriorRing();
    return encodeLineString(exteriorRing);
  }
  /**
   * Encodes an integer value to a polyline string.
   *
   * @param value the integer value to encode
   * @return the encoded polyline string
   */
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

  @Deprecated
  public static String polygonStringToFoursquareFormat(Polygon polygon) {

    int step = 75;
    Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
    StringBuilder formattedString = new StringBuilder();

    // iterate over coordinates skipping {step} coordinates each time
    for (int i = 0; i < coordinates.length; i += step) {
      // use CoordinateFormatter to trim lat long to 3dp
      formattedString
              .append(CoordinateFormatter.formatCoordinate(coordinates[i].y))
              .append(",")
              .append(CoordinateFormatter.formatCoordinate(coordinates[i].x));

      // append '~' separator except after last coordinate
      if (i + step < coordinates.length) {
        formattedString.append("~");
      }
    }

    // ensure the polygon is closed (first and last coordinates should be the same)
    if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
      formattedString
              .append("~")
              .append(CoordinateFormatter.formatCoordinate(coordinates[0].y))
              .append(",")
              .append(CoordinateFormatter.formatCoordinate(coordinates[0].x));
    }

    return formattedString.toString();
  }

}
