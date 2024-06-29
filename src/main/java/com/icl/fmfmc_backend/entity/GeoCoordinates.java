package com.icl.fmfmc_backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;

@Embeddable
@Data
@NoArgsConstructor
//@AllArgsConstructor
public class GeoCoordinates {
    private Double latitude;
    private Double longitude;
//
//    @JsonCreator
//    public static GeoCoordinates create(JsonNode jsonNode) {
//        GeoCoordinates geoCoordinates = new GeoCoordinates();
//        JsonNode latNode = jsonNode.at("/main/latitude");
//        JsonNode lonNode = jsonNode.at("/main/longitude");
//        if (!latNode.isMissingNode()) {
//            geoCoordinates.setLatitude(latNode.asDouble());
//        }
//        if (!lonNode.isMissingNode()) {
//            geoCoordinates.setLongitude(lonNode.asDouble());
//        }
//        return geoCoordinates;
//    }

    public GeoCoordinates(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        validate();
    }

    private void validate() {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude values");
        }
    }

    public Coordinate toJtsCoordinate() {
        return new Coordinate(this.longitude, this.latitude);
    }

}
