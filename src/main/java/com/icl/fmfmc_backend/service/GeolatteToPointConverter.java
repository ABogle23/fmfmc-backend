package com.icl.fmfmc_backend.service;

import org.geolatte.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GeolatteToPointConverter {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  public static org.locationtech.jts.geom.Point convert(Point source) {
    return geometryFactory.createPoint(
        new Coordinate(
            source.getPosition().getCoordinate(0), source.getPosition().getCoordinate(1)));
  }

}
