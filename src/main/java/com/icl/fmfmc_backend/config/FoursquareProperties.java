package com.icl.fmfmc_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FoursquareProperties {

  @Value("${foursquare.api.key}")
  private String apiKey;

  @Value("${foursquare.api.base-url}")
  private String baseUrl;

  public String getApiKey() {
    return apiKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
