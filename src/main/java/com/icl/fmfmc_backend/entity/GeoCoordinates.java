package com.icl.fmfmc_backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        validate();
    }

    private void validate() {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude values");
        }
    }

}
