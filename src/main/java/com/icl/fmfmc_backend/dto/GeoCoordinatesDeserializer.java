package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.icl.fmfmc_backend.entity.GeoCoordinates;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GeoCoordinatesDeserializer extends JsonDeserializer<List<GeoCoordinates>> {

  @Override
  public List<GeoCoordinates> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    List<List<Double>> coordinatesList = p.readValueAs(new TypeReference<List<List<Double>>>() {});
    return coordinatesList.stream()
        .map(coordinates -> new GeoCoordinates(coordinates.get(0), coordinates.get(1)))
        .collect(Collectors.toList());
  }
}
