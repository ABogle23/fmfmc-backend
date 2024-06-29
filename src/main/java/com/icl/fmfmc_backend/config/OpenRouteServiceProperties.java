package com.icl.fmfmc_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenRouteServiceProperties {

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    @Value("${openrouteservice.directionsservice.geoson.api.base-url}")
    private String OSRDirectionsServiceBaseUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getOSRDirectionsServiceBaseUrl() {
        return OSRDirectionsServiceBaseUrl;
    }

}
