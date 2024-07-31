package com.icl.fmfmc_backend.config;

import com.icl.fmfmc_backend.integration.DirectionsClient;
import com.icl.fmfmc_backend.integration.MapboxDirectionsClient;
import com.icl.fmfmc_backend.integration.OsrDirectionsClient;
import com.icl.fmfmc_backend.Routing.GeometryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DirectionsClientConfig {
  @Bean("osrClient")
  @Primary
  public DirectionsClient osrClient(
      OpenRouteServiceProperties openRouteServiceProperties, GeometryService geometryService) {
    return new OsrDirectionsClient(openRouteServiceProperties, geometryService);
  }

  @Bean
  public OpenRouteServiceProperties openRouteServiceProperties() {
    return new OpenRouteServiceProperties();
  }

  // can add additional client here e.g. for mapbox or google

  @Bean("mapboxClient")
  public DirectionsClient mapboxClient(
          MapboxProperties mapboxProperties) {
    return new MapboxDirectionsClient(mapboxProperties);
  }

  @Bean
  public MapboxProperties mapboxProperties() {
    return new MapboxProperties();
  }



}
