package com.icl.fmfmc_backend.config;

import com.icl.fmfmc_backend.Integration.DirectionsClient;
import com.icl.fmfmc_backend.Integration.OsrDirectionsClient;
import com.icl.fmfmc_backend.Routing.GeometryService;
import org.springframework.context.annotation.Bean;

public class DirectionsClientConfig {
  @Bean
  public DirectionsClient osrClient(
      OpenRouteServiceProperties openRouteServiceProperties, GeometryService geometryService) {
    return new OsrDirectionsClient(openRouteServiceProperties, geometryService);
  }

  @Bean
  public OpenRouteServiceProperties openRouteServiceProperties() {
    return new OpenRouteServiceProperties();
  }

  // can add additional client here e.g. for mapbox or google

}
