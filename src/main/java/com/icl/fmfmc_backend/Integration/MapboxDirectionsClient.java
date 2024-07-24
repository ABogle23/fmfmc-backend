package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.config.MapboxProperties;
import com.icl.fmfmc_backend.dto.Api.MapboxRequest;
import com.icl.fmfmc_backend.dto.Api.MapboxResponse;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.exception.CommonResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class MapboxDirectionsClient implements DirectionsClient {

  private static final Logger logger = LoggerFactory.getLogger(MapboxDirectionsClient.class);
  private final MapboxProperties mapboxProperties;

  private final WebClient.Builder webClientBuilder = WebClient.builder();

  //  @Override
  //  public DirectionsResponse getDirections(DirectionsRequest request) {
  //    return null;
  //  }

  @Override
  public DirectionsResponse getDirections(DirectionsRequest directionsRequest) {
    MapboxRequest requestDto = mapToMapBoxRequest(directionsRequest);
    String coordinatesString = formatCoordinates(requestDto.getCoordinates());

    System.out.println("Coordinates string: " + coordinatesString);

    int timeoutSeconds = 10;

    WebClient webClient = buildWebClient();

    logger.info("Attempting to fetch directions from Mapbox");

    String fullUrl = buildUrl(coordinatesString);

    logger.info("Attempting to fetch directions from Mapbox, URL: {}", fullUrl);

    return webClient
        .get()
        .uri(fullUrl)
        .exchangeToMono(
            response -> CommonResponseHandler.handleResponse(response, MapboxResponse.class))
//        .timeout(Duration.ofSeconds(timeoutSeconds))
//        .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(5)))
        .map(this::processDirectionsResponse)
        .doOnSuccess(resp -> logger.info("Successfully fetched and processed directions"))
        .doOnError(error -> logger.error("Error fetching directions: {}", error.getMessage()))
        .block();
  }

  private String buildUrl(String coordinatesString) {
    return UriComponentsBuilder.fromHttpUrl(mapboxProperties.getBaseUrl())
        .path("/{coordinates}")
        .queryParam("access_token", mapboxProperties.getApiKey())
        .queryParam("geometries", "geojson")
        .queryParam("overview", "full")
        .buildAndExpand(coordinatesString)
        .toUriString();
  }

  private MapboxRequest mapToMapBoxRequest(DirectionsRequest directionsRequest) {

    List<Double[]> coordinates = directionsRequest.getCoordinates();
    System.out.println("Coordinates: " + coordinates);

    return new MapboxRequest(coordinates);
  }

  private WebClient buildWebClient() {
    int bufferSize = 16 * 1024 * 1024; // Increase buffer size to 16 MB

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(
                clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(bufferSize))
            .build();

    WebClient webClient =
        WebClient.builder()
            .baseUrl(mapboxProperties.getBaseUrl())
            .exchangeStrategies(strategies)
            .build();

    return webClient;
  }

  private DirectionsResponse processDirectionsResponse(MapboxResponse mapboxResponse) {

    System.out.println("Mapbox response: " + mapboxResponse);

    DirectionsResponse directionsResponse = new DirectionsResponse();

    setDurationsAndDistances(mapboxResponse, directionsResponse);

    directionsResponse.setLineString(
        mapboxResponse.getRoutes().get(0).getGeometry().getCoordinates());
    directionsResponse.setTotalDistance(mapboxResponse.getRoutes().get(0).getDistance());
    directionsResponse.setTotalDuration(mapboxResponse.getRoutes().get(0).getDuration());

    return directionsResponse;
  }

  private String formatCoordinates(List<Double[]> coordinates) {
    return coordinates.stream()
        .map(
            coordinate ->
                coordinate[0] + "," + coordinate[1]) // Ensure the order is longitude,latitude
        .collect(Collectors.joining(";"));
  }

  public void setDurationsAndDistances(
      MapboxResponse mapboxResponse, DirectionsResponse directionsResponse) {
    // set individual distances and durations per leg.
    List<Double> durations = new ArrayList<>();
    List<Double> distances = new ArrayList<>();

    for (MapboxResponse.RouteDTO route : mapboxResponse.getRoutes()) {
      for (MapboxResponse.LegDTO leg : route.getLegs()) {
        durations.add(leg.getDuration());
        distances.add(leg.getDistance());
      }
    }

    directionsResponse.setLegDurations(durations);
    directionsResponse.setLegDistances(distances);
  }
}
