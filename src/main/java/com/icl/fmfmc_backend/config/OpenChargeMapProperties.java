package com.icl.fmfmc_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenChargeMapProperties {

  @Value("${openchargemap.api.key}")
  private String apiKey;

  @Value("${openchargemap.api.base-url}")
  private String baseUrl;

  @Value("${openchargemap.api.client}")
  private String client;

  @Value("${openchargemap.api.countrycode}")
  private String countryCode;

  @Value("${openchargemap.api.camelcase}")
  private boolean camelCase;

  @Value("${openchargemap.api.compact}")
  private boolean compact;

  @Value("${openchargemap.api.distanceunit}")
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
