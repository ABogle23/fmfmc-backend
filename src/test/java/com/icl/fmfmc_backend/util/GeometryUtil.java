package com.icl.fmfmc_backend.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class GeometryUtil {
  private static final GeometryFactory geometryFactory = new GeometryFactory();

  public static LineString createLineString(String coordinateString) {
    String[] parts = coordinateString.split(", ");
    Coordinate[] coordinates = new Coordinate[parts.length];
    for (int i = 0; i < parts.length; i++) {
      String[] lonLat = parts[i].split(" ");
      coordinates[i] = new Coordinate(Double.parseDouble(lonLat[0]), Double.parseDouble(lonLat[1]));
    }
    return geometryFactory.createLineString(coordinates);
  }
}
