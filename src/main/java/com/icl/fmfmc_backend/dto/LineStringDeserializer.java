package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineStringDeserializer extends JsonDeserializer<LineString> {

    @Override
    public LineString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<Coordinate> coordinates = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode coordNode : node) {
                if (coordNode.isArray() && coordNode.size() >= 2) {
                    double lon = coordNode.get(0).asDouble();
                    double lat = coordNode.get(1).asDouble();
                    coordinates.add(new Coordinate(lon, lat));
                }
            }
        }
        GeometryFactory factory = new GeometryFactory();
        return factory.createLineString(coordinates.toArray(new Coordinate[0]));
    }
}