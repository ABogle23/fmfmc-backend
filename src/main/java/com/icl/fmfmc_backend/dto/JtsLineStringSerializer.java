package com.icl.fmfmc_backend.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;

@Deprecated
public class JtsLineStringSerializer extends JsonSerializer<LineString> {

    @Override
    public void serialize(LineString value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", "LineString");
        jsonGenerator.writeFieldName("coordinates");
        jsonGenerator.writeStartArray();
        for (Coordinate coord : value.getCoordinates()) {
            jsonGenerator.writeArray(new double[]{coord.x, coord.y}, 0, 2);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}