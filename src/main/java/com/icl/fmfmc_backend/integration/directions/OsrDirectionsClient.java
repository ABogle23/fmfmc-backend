package com.icl.fmfmc_backend.integration.directions;

import com.icl.fmfmc_backend.geometry_service.GeometryService;
import com.icl.fmfmc_backend.config.integration.OpenRouteServiceProperties;
import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.dto.directions.OSRDirectionsServiceGeoJSONRequest;
import com.icl.fmfmc_backend.dto.directions.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.exception.handler.CommonResponseHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Service class for interacting with the OpenRouteService Directions API. */
@RequiredArgsConstructor
@Slf4j
@Service
public class OsrDirectionsClient implements DirectionsClient {

  private static final Logger logger = LoggerFactory.getLogger(OsrDirectionsClient.class);
  private final OpenRouteServiceProperties orsProperties;
  private final GeometryService geometryService;

  private final WebClient.Builder webClientBuilder = WebClient.builder();

  /**
   * Retrieves directions from the OpenRouteService Directions API based on the provided request.
   *
   * @param directionsRequest the DirectionsRequest object containing the request details
   * @return a DirectionsResponse object containing the directions
   */
  @Override
  public DirectionsResponse getDirections(DirectionsRequest directionsRequest) {

    OSRDirectionsServiceGeoJSONRequest requestDto =
        mapToOSRDirectionsServiceGeoJSONRequest(directionsRequest);

    int bufferSize = 16 * 1024 * 1024; // 16 MB
    int timeoutSeconds = 10;

    WebClient webClient = buildWebClient();

    String baseUrl = orsProperties.getOSRDirectionsServiceBaseUrl();
    //        logger.info("Base URL from properties: {}", baseUrl);

    logger.info("Attempting to fetch directions from OpenRouteService");

    return webClient
        .post()
        .uri("")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(requestDto), OSRDirectionsServiceGeoJSONRequest.class)
        .exchangeToMono(
            response ->
                CommonResponseHandler.handleResponse(
                    response, OSRDirectionsServiceGeoJSONResponse.class))
        //        .timeout(Duration.ofSeconds(timeoutSeconds)) // Set the timeout
        //        .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(5))) // Set the retry policy
        .map(this::processDirectionsResponse)
        .doOnSuccess(
            response -> {
              logger.info("Successfully fetched and processed directions");
            })
        .doOnError(error -> logger.error("Error fetching directions: {}", error.getMessage()))
        .block();
  }

  private OSRDirectionsServiceGeoJSONRequest mapToOSRDirectionsServiceGeoJSONRequest(
      DirectionsRequest directionsRequest) {

    List<Double[]> coordinates = directionsRequest.getCoordinates();

    return new OSRDirectionsServiceGeoJSONRequest(coordinates);
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

  private DirectionsResponse processDirectionsResponse(
      OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse) {

    DirectionsResponse directionsResponse = new DirectionsResponse();

    setDurationsAndDistances(osrDirectionsServiceGeoJSONResponse, directionsResponse);

    directionsResponse.setLineString(
        geometryService.createLineString(
            osrDirectionsServiceGeoJSONResponse
                .getFeatures()
                .get(0)
                .getGeometry()
                .getCoordinates()));
    directionsResponse.setTotalDistance(
        osrDirectionsServiceGeoJSONResponse
            .getFeatures()
            .get(0)
            .getProperties()
            .getSummary()
            .getDistance());
    directionsResponse.setTotalDuration(
        osrDirectionsServiceGeoJSONResponse
            .getFeatures()
            .get(0)
            .getProperties()
            .getSummary()
            .getDuration());

    return directionsResponse;
  }

  public void setDurationsAndDistances(
      OSRDirectionsServiceGeoJSONResponse response, DirectionsResponse directionsResponse) {
    // set individual distances and durations per leg.
    List<Double> durations = new ArrayList<>();
    List<Double> distances = new ArrayList<>();

    for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO feature : response.getFeatures()) {
      for (OSRDirectionsServiceGeoJSONResponse.FeatureDTO.PropertiesDTO.SegmentDTO segment :
          feature.getProperties().getSegments()) {
        durations.add(segment.getDuration());
        distances.add(segment.getDistance());
      }
    }

    directionsResponse.setLegDurations(durations);
    directionsResponse.setLegDistances(distances);
  }
}
