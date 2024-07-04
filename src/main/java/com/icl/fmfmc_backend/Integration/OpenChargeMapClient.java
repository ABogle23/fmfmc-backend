package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.config.OpenChargeMapProperties;
import com.icl.fmfmc_backend.dto.OpenChargeMapResponseDTO;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.Charger;
import com.icl.fmfmc_backend.entity.Connection;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.service.ChargerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenChargeMapClient {
  private static final Logger logger = LoggerFactory.getLogger(OpenChargeMapClient.class);
  private final OpenChargeMapProperties openChargeMapProperties;
  private final ChargerService chargerService;

  public void getChargerFromOpenChargeMapApi(MultiValueMap<String, String> parameters) {
    int bufferSize = 64 * 1024 * 1024; // 64 MB
    int timeoutSeconds = 180;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(
                clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(bufferSize))
            .build();

    WebClient webClient =
        WebClient.builder()
            .baseUrl(openChargeMapProperties.getBaseUrl())
            .exchangeStrategies(strategies)
            .build();

    logger.info("Attempting to fetch chargers");

    List<OpenChargeMapResponseDTO> response = webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .queryParam("key", openChargeMapProperties.getApiKey())
                        .queryParam("client", openChargeMapProperties.getClient())
                        .queryParam("countrycode", openChargeMapProperties.getCountryCode())
                        .queryParam("camelcase", openChargeMapProperties.getCamelCase())
                        .queryParam("compact", openChargeMapProperties.getCompact())
                            .queryParam("distanceunit", openChargeMapProperties.getDistanceUnit())
                            .queryParam("verbose", false)
                        .queryParam("maxresults", 2000)
                        // 50.719334, -3.513779 2.7 Exeter Centre
                                                .queryParam("latitude", 50.267355)
                                                    .queryParam("longitude", -4.060051)
                                                    .queryParam("distance", 129)
//                        .queryParam(
//                            "polygon",
//                            "y`yoHnd}]wa`@wn`G_n}AcvwEwp{Bj{dAmuCn_bGdn~ErzoH~cb@itcAhaAgppA")
                        .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<OpenChargeMapResponseDTO>>() {})
                .timeout(Duration.ofSeconds(timeoutSeconds))
//            .retryWhen(
//                Retry.fixedDelay(
//                    3,
//                    Duration.ofSeconds(
//                        10))) // Retry up to 3 times, waiting 5 seconds between attempts
            .block();

    if (response != null && !response.isEmpty()) {
      response.forEach(dto -> {
        Charger charger = convertToCharger(dto);
        try {
          chargerService.saveCharger(charger);
          logger.info("Charger processed and saved successfully.");
        } catch (Exception e) {
          logger.error("Error saving charger: {}", e.getMessage());
        }
      });
    } else {
      logger.error("No data received from OpenChargeMap API");
    }
  }

  private Charger convertToCharger(OpenChargeMapResponseDTO dto) {
    Charger charger = new Charger();
    charger.setId(dto.getId());
    charger.setUuid(dto.getUuid());
    charger.setRecentlyVerified(dto.isRecentlyVerified());
    charger.setDateLastVerified(dto.getDateLastVerified());
    charger.setDateLastStatusUpdate(dto.getDateLastStatusUpdate());
    charger.setDateCreated(dto.getDateCreated());
    charger.setUsageCost(dto.getUsageCost());
    charger.setDataProviderID(dto.getDataProviderID());
    charger.setNumberOfPoints(dto.getNumberOfPoints() != null ? dto.getNumberOfPoints() : 1L);
    charger.setUsageTypeID(dto.getUsageTypeID());
    charger.setSubmissionStatusTypeID(dto.getSubmissionStatusTypeID());
    charger.setStatusTypeID(dto.getStatusTypeID());
    charger.setOperatorID(dto.getOperatorID());
    charger.setOperatorsReference(dto.getOperatorsReference());
    charger.setCreatedAt(dto.getCreatedAt());
    charger.setUpdatedAt(dto.getUpdatedAt());
    charger.setAddressInfo(new AddressInfo(dto.getId().toString(), dto.getLocation().getAddressLine1(), dto.getLocation().getTown(), dto.getLocation().getPostcode()));
    charger.setConnections(dto.getConnections().stream()
            .map(conn -> new Connection(conn.getId(), conn.getReference(), conn.getConnectionTypeID(), conn.getCurrentTypeID(), conn.getAmps(), conn.getVoltage(), conn.getPowerKW(), conn.getQuantity(), conn.getStatusTypeID(), conn.getStatusType(), conn.getLevelID(), conn.getCreatedAt(), conn.getUpdatedAt()))
            .collect(Collectors.toList()));
    charger.setGeocodes(new GeoCoordinates(dto.getLocation().getLongitude(), dto.getLocation().getLatitude()));
    charger.setLocation(new GeometryFactory().createPoint(new Coordinate(dto.getLocation().getLongitude(), dto.getLocation().getLatitude())));
    return charger;
  }


  //    return chargerService.saveCharger(charger);
}

//
//List<Charger> chargers = chargerMono.block();
//    if (chargers == null || chargers.isEmpty()) {
//        logger.error("No charger data received from OpenChargeMap API");
//      return null;
//              } else {
//              // Print the size of the response
//              logger.info("Size of the OpenChargeMap API response: {}", chargers.size());
//        }
//
//        // Save each charger to the database
//        for (Charger charger : chargers) {
//        try {
//        chargerService.saveCharger(charger);
//      } catch (DataIntegrityViolationException e) {
//        logger.error("Error saving charger with id {}: {}", charger.getId(), e.getMessage());
//        }
//        }