package com.icl.fmfmc_backend.service;

import org.geolatte.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// used by chargerRepo query getChargerLocationsByParams to convert geolatte point to jts point
@Component
public class GeolatteToPointConverter {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  /**
   * Converts a Geolatte Point to a JTS Point.
   *
   * @param source the Geolatte Point to convert
   * @return the converted JTS Point
   */
  public static org.locationtech.jts.geom.Point convert(Point source) {
    return geometryFactory.createPoint(
        new Coordinate(
            source.getPosition().getCoordinate(0), source.getPosition().getCoordinate(1)));
  }
}
