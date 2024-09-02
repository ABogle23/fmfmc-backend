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

/** Custom deserializer for converting JSON arrays into LineString objects. */
public class LineStringDeserializer extends JsonDeserializer<LineString> {

  /**
   * Deserializes JSON content into a LineString object.
   *
   * @param jsonParser the JsonParser used to parse the JSON content
   * @param context the DeserializationContext
   * @return a LineString object created from the JSON content
   * @throws IOException if an I/O error occurs during deserialization
   */
  @Override
  public LineString deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
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
