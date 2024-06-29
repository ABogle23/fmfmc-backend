package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.config.OpenRouteServiceProperties;
import com.icl.fmfmc_backend.dto.OSRDirectionsServiceGeoJSONRequest;
import com.icl.fmfmc_backend.dto.OSRDirectionsServiceGeoJSONResponse;
import com.icl.fmfmc_backend.util.CommonResponseHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Slf4j
@Service
public class OSRService {

    private static final Logger logger = LoggerFactory.getLogger(OSRService.class);
    private final OpenRouteServiceProperties orsProperties;

    private final WebClient.Builder webClientBuilder = WebClient.builder();

    public OSRDirectionsServiceGeoJSONResponse getDirectionsGeoJSON(OSRDirectionsServiceGeoJSONRequest requestDto) {
        WebClient webClient = buildWebClient();

        String baseUrl = orsProperties.getOSRDirectionsServiceBaseUrl();
        logger.info("Base URL from properties: {}", baseUrl);

        logger.info("Attempting to fetch directions from OpenRouteService");

        return webClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestDto), OSRDirectionsServiceGeoJSONRequest.class)
                .exchangeToMono(response -> CommonResponseHandler.handleResponse(response, OSRDirectionsServiceGeoJSONResponse.class))
                .doOnSuccess(response -> {
                    logger.info("Successfully fetched and processed directions");
                    processDirectionsResponse(response);
                })
                .doOnError(error -> logger.error("Error fetching directions: {}", error.getMessage()))
                .block();
    }

    private WebClient buildWebClient() {
        // Add headers e.g. api key
        return webClientBuilder
                .baseUrl(orsProperties.getOSRDirectionsServiceBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION,orsProperties.getApiKey())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .build();
    }

    private OSRDirectionsServiceGeoJSONResponse processDirectionsResponse(OSRDirectionsServiceGeoJSONResponse response) {
        // TODO process response
        System.out.println(response);
        logger.info("Successfully fetched and processed directions");
        return response;
    }

}
