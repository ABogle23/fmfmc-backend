package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.config.FoursquareProperties;
import com.icl.fmfmc_backend.dto.FoodEstablishment.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.FoodEstablishment.Category;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishmentRequest;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequest;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// https://docs.foursquare.com/developer/reference/place-search

@Service
@RequiredArgsConstructor
@Slf4j
public class FoursquareClient implements FoodEstablishmentClient {

  private static final Logger logger = LoggerFactory.getLogger(FoursquareClient.class);
  private final FoursquareProperties foursquarePlacesProperties;

  @Override
  public List<FoodEstablishment> getFoodEstablishmentsByParam(
      FoodEstablishmentRequest foodEstablishmentRequest) {
    int bufferSize = 16 * 1024 * 1024; // 16 MB
    int timeoutSeconds = 180;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(
                clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(bufferSize))
            .build();

    WebClient webClient =
        WebClient.builder()
            .baseUrl(foursquarePlacesProperties.getBaseUrl())
            .defaultHeader("Authorization", foursquarePlacesProperties.getApiKey())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(strategies)
            .build();

    logger.info("Attempting to fetch foodEstablishments");

    FoursquareResponseDTO foursquareResponseDTO =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder.queryParams(foodEstablishmentRequest.getQueryParams()).build())
            .retrieve()
            .bodyToMono(FoursquareResponseDTO.class)
            .block();
    //        .timeout(Duration.ofSeconds(timeoutSeconds));
    //            .retryWhen(
    //                Retry.fixedDelay(
    //                    3,
    //                    Duration.ofSeconds(
    //                        10))) // Retry up to 3 times, waiting 5 seconds between
    // attempts
    //            .doOnSuccess(
    //                    response -> {
    //                      if (response != null && response.getResults() != null) {
    //                        List<FoodEstablishment> processedEstablishments =
    // response.getResults().stream()
    //                                .map(this::processFoodEstablishment)
    //                                .collect(Collectors.toList());
    //                        establishments.addAll(processedEstablishments);
    //                      }
    //                    })
    //            .doOnError(
    //                    error -> logger.error("Error fetching food establishments: {}",
    // error.getMessage()))
    //            .block();
    logger.info("Fetched foodEstablishments from Foursquare");
    logger.info(
        "Fetched foodEstablishments from Foursquare: {}", foursquareResponseDTO.getResults());

    List<FoodEstablishment> foodEstablishments = processFoursquareResponse(foursquareResponseDTO);

    return foodEstablishments;
  }

  public List<FoodEstablishment> processFoursquareResponse(FoursquareResponseDTO response) {

    List<FoodEstablishment> foodEstablishments = new ArrayList<>();

    if (response != null && response.getResults() != null) {
      response
          .getResults()
          .forEach(
              placeDTO -> {
                FoodEstablishment establishment = convertToFoodEstablishment(placeDTO);
                foodEstablishments.add(establishment);
              });
    }
    log.info("Fetched {} food establishments from Foursquare", foodEstablishments.size());
    return foodEstablishments;
  }

  private FoodEstablishment convertToFoodEstablishment(
      FoursquareResponseDTO.FoursquarePlaceDTO dto) {
    FoodEstablishment establishment = new FoodEstablishment();
    establishment.setId(dto.getFsqId());
    establishment.setName(dto.getName());
    establishment.setAddress(
        new AddressInfo(
            dto.getFsqId(),
            dto.getLocation().getFormattedAddress(),
            dto.getLocation().getLocality(),
            dto.getLocation().getPostcode()));
    establishment.setCategories(
        dto.getCategories().stream()
            .map(cat -> new Category(cat.getId(), cat.getName()))
            .collect(Collectors.toList()));
    establishment.setGeocodes(
        new GeoCoordinates(
            dto.getGeocodes().getMain().getLongitude(), dto.getGeocodes().getMain().getLatitude()));
    establishment.setClosedStatus(dto.getClosedBucket());
    establishment.setPopularity(dto.getPopularity());
    establishment.setPrice(dto.getPrice());
    establishment.setRating(dto.getRating());
    establishment.setCreatedAt(LocalDateTime.now());
    establishment.setLocation(
        new GeometryFactory()
            .createPoint(
                new Coordinate(
                    dto.getGeocodes().getMain().getLongitude(),
                    dto.getGeocodes().getMain().getLatitude())));
    establishment.setWebsite(dto.getWebsite());

    return establishment;
  }
}
