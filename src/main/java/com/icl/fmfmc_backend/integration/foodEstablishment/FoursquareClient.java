package com.icl.fmfmc_backend.integration.foodEstablishment;

import com.icl.fmfmc_backend.config.integration.FoursquareProperties;
import com.icl.fmfmc_backend.dto.foodEstablishment.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.foodEstablishment.Category;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.dto.foodEstablishment.FoodEstablishmentRequest;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.exception.handler.CommonResponseHandler;
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
            .exchangeToMono(
                response ->
                    CommonResponseHandler.handleResponse(response, FoursquareResponseDTO.class))
            .block();

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

//    Double rating = dto.getRating();
//    if (dto.getRating() != null) {
//      return null;
//    }

    Double rating = dto.getRating() != null ? (dto.getRating() / 10) : null;

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
    establishment.setRating(rating);
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
