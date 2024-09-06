package com.icl.fmfmc_backend.Routing;

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.service.CoordinateFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolylineUtility {

  /**
   * Encodes a LineString to a polyline string.
   *
   * @param lineString the LineString to encode
   * @return the encoded polyline string
   */
  public static String encodeLineString(LineString lineString) {
    List<LatLng> path = new ArrayList<>();

    for (Coordinate coord : lineString.getCoordinates()) {
      path.add(new LatLng(coord.y, coord.x)); // JTS uses long lat, google uses lat long
    }

    return PolylineEncoding.encode(path);
  }

  /**
   * Encodes a Polygon to a polyline string.
   *
   * @param polygon the Polygon to encode
   * @return the encoded polyline string
   */
  public static String encodePolygon(Polygon polygon) {
    List<LatLng> path = new ArrayList<>();
    Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();

    for (Coordinate coord : coordinates) {
      path.add(new LatLng(coord.y, coord.x)); // JTS uses long lat, google uses lat long
    }

    if (!path.get(0).equals(path.get(path.size() - 1))) {
      path.add(path.get(0));
    }

    return PolylineEncoding.encode(path);
  }

}
