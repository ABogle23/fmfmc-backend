package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.config.OpenRouteServiceProperties;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Slf4j
@Service
public class MapboxDirectionsClient implements DirectionsClient {

  private static final Logger logger = LoggerFactory.getLogger(MapboxDirectionsClient.class);
  private final OpenRouteServiceProperties orsProperties;
  private final GeometryService geometryService;

  private final WebClient.Builder webClientBuilder = WebClient.builder();

  @Override
  public DirectionsResponse getDirections(DirectionsRequest request) {
    return null;
  }







  private WebClient buildWebClient() {
    int bufferSize = 16 * 1024 * 1024; // Increase buffer size to 16 MB

    ExchangeStrategies exchangeStrategies =
            ExchangeStrategies.builder()
                    .codecs(
                            clientCodecConfigurer -> {
                              clientCodecConfigurer.defaultCodecs().maxInMemorySize(bufferSize);
                            })
                    .build();

    return webClientBuilder
            .baseUrl(orsProperties.getOSRDirectionsServiceBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, orsProperties.getApiKey())
            .defaultHeader(
                    HttpHeaders.ACCEPT,
                    "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
            .exchangeStrategies(exchangeStrategies)
            .build();
  }

}
