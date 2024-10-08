package com.icl.fmfmc_backend.config.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapboxProperties {

  @Value("${mapbox.api.key}")
  private String apiKey;

  @Value("${mapbox.api.base-url}")
  private String mapboxDirectionsServiceBaseUrl;

  public String getApiKey() {
    return apiKey;
  }

  public String getBaseUrl() {
    return mapboxDirectionsServiceBaseUrl;
  }
}
