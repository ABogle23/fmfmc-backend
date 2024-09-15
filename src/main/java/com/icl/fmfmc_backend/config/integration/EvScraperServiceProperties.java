package com.icl.fmfmc_backend.config.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EvScraperServiceProperties {

  @Value("${evscraperservice.base-url}")
  private String baseUrl;

  public String getBaseUrl() {
    return baseUrl;
  }

}
