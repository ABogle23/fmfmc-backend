package com.icl.fmfmc_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "app.directions")
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
