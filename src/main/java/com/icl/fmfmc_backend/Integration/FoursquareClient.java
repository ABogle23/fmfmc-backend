package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.config.FoursquareProperties;
import com.icl.fmfmc_backend.dto.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.*;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;



//https://docs.foursquare.com/developer/reference/place-search


@Service
@RequiredArgsConstructor
@Slf4j
public class FoursquareClient {

  private static final Logger logger = LoggerFactory.getLogger(FoursquareClient.class);
  private final FoursquareProperties foursquarePlacesProperties;
  private final FoodEstablishmentService foodEstablishmentService;

  //    public FoursquareService(
  //            FoursquareProperties foursquarePlacesProperties, FoodEstablishmentService
  // chargerService) {
  //
  //        this.foursquarePlacesProperties = foursquarePlacesProperties;
  //        this.foodEstablishmentService = chargerService;
  //    }

  public void getFoodEstablishmentFromFoursquarePlacesApi(
      MultiValueMap<String, String> parameters) {
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

    webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParams(parameters)
                    .queryParam("limit", 5)
                    // 50.719334, -3.513779 2.7 Exeter Centre
                    .queryParam("ll", "50.267355,-4.060051")
                    .queryParam("radius", 20000) // metres
                    .queryParam("categories", "13065")
                    .queryParam(
                        "fields",
                        "fsq_id,name,categories,closed_bucket,distance,geocodes,location,price,rating,popularity")
                    //                        .queryParam(
                    //                            "polygon",
                    //
                    // "y`yoHnd}]wa`@wn`G_n}AcvwEwp{Bj{dAmuCn_bGdn~ErzoH~cb@itcAhaAgppA")
                    .build())
        .retrieve()
        .bodyToMono(FoursquareResponseDTO.class)
        .timeout(Duration.ofSeconds(timeoutSeconds))
        //            .retryWhen(
        //                Retry.fixedDelay(
        //                    3,
        //                    Duration.ofSeconds(
        //                        10))) // Retry up to 3 times, waiting 5 seconds between
        // attempts
        .doOnSuccess(
            response -> {
              if (response != null && response.getResults() != null) {
                response.getResults().forEach(this::processFoodEstablishment);
              }
            })
        .doOnError(
            error -> logger.error("Error fetching food establishments: {}", error.getMessage()))
        .block();
  }

  private void processFoodEstablishment(FoursquareResponseDTO.FoursquarePlaceDTO place) {
    FoodEstablishment establishment = convertToFoodEstablishment(place);
    foodEstablishmentService.saveFoodEstablishment(establishment);
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
    establishment.setCategories(dto.getCategories().stream()
            .map(cat -> new Category(cat.getId(), cat.getName()))
            .collect(Collectors.toList()));
    establishment.setGeocodes(
        new GeoCoordinates(dto.getGeocodes().getMain().getLongitude(), dto.getGeocodes().getMain().getLatitude()));
    establishment.setClosedStatus(dto.getClosedBucket());
    establishment.setPopularity(dto.getPopularity());
    establishment.setPrice(dto.getPrice());
    establishment.setRating(dto.getRating());
    establishment.setCreatedAt(LocalDateTime.now());
    establishment.setLocation(new GeometryFactory().createPoint(new Coordinate(dto.getGeocodes().getMain().getLongitude(), dto.getGeocodes().getMain().getLatitude())));
    return establishment;
  }
}
