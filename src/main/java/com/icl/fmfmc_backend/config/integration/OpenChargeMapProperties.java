package com.icl.fmfmc_backend.config.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenChargeMapProperties {

  @Value("${openchargemap.api.key}")
  private String apiKey;

  @Value("${openchargemap.api.base-url}")
  private String baseUrl;

  @Value("fmfmc-backend")
  private String client;

  @Value("GB")
  private String countryCode;

  @Value("true")
  private boolean camelCase;

  @Value("true")
  private boolean compact;

  @Value("km")
  private String distanceUnit;

  public String getApiKey() {
    return apiKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getClient() {
    return client;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public Boolean getCamelCase() {
    return camelCase;
  }

  public Boolean getCompact() {
    return compact;
  }

  public String getDistanceUnit() {
    return distanceUnit;
  }
}
