package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.api.*;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.service.JourneyService;

import com.icl.fmfmc_backend.util.LogExecutionTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/** REST controller for managing journeys. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Journey", description = "Journey Controller")
public class JourneyController {

  private final JourneyService journeyService;

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);

  /**
   * Endpoint to get a journey complete with route, charging stations, and any suitable food
   * establishments along the way.
   *
   * @param routeRequest the route request object containing journey parameters
   * @return ResponseEntity containing the journey details
   * @throws JourneyNotFoundException if the journey is not found
   */
  @DocsResponseCodes
  @Operation(
      summary = "Get a Journey",
      description =
          "Returns a journey complete with route, charging stations, and any suitable food establishments along the way.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Route request object containing journey parameters",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = RouteRequest.class))),
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description =
                "Bad Request - The request could not be understood and therefore could not be processed",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @LogExecutionTime(message = "Response time for /find-route endpoint ")
  @PostMapping("/find-route")
  public ResponseEntity<?> getJourney(@Valid @RequestBody RouteRequest routeRequest)
      throws JourneyNotFoundException {
    logger.info("Received route request: {}", routeRequest);
    logger.info("Journey is valid");
    JourneyContext context = new JourneyContext();
    RouteResult routeResult = null;

    routeResult = journeyService.getJourney(routeRequest, context);

    ApiResponse<RouteResult> response =
        new ApiResponse<>(
            routeResult,
            true,
            "Route successfully calculated.",
            context.getFallbackUsed() ? true : null,
            context.getFallbackUsed()
                ? "Fallback strategies applied: " + context.getFallbackMessageAsString()
                : null);

    return ResponseEntity.ok(response);
  }
}
