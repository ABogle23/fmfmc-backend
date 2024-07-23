package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.Api.ApiResponse;
import com.icl.fmfmc_backend.dto.Api.JourneyContext;
import com.icl.fmfmc_backend.service.JourneyService;
import com.icl.fmfmc_backend.entity.enums.FallbackStrategy;


import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
//@Validated
public class JourneyController {

  /**
   * This method is called when a GET request is made URL: localhost:8080/route/find-route Purpose:
   * Fetches all the chargers in the FoodEstablishments
   *
   * @return Route
   */

  private final JourneyService journeyService;

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);

  @LogExecutionTime(message = "Response time for /find-route endpoint ")

  @PostMapping("/find-route")
  public ResponseEntity<?> getJourney(@Valid @RequestBody RouteRequest routeRequest) {
    logger.info("Received route request: {}", routeRequest);
    logger.info("Journey is valid");
    JourneyContext context = new JourneyContext();
    RouteResult routeResult = journeyService.getJourney(routeRequest, context);

    /* TODO: handle situation where no route can be found given the constraints
             perhaps return a "404 with a message saying no route can be found"
             or relax the constraints (to defaults maybe) and try again on the
             clients behalf with a message "No valid route found, this is the
             best attempt"   */

    String fallbackMessage = context.getFallbackStrategies().stream()
            .map(FallbackStrategy::getDescription)
            .collect(Collectors.joining(", "));
    ApiResponse<RouteResult> response = new ApiResponse<>(
            routeResult,
            true,
            "Route successfully calculated.",
            context.getFallbackUsed(),
            context.getFallbackUsed() ? "Fallback strategies applied: " + fallbackMessage : null
    );
    return ResponseEntity.ok(response);



//    return ResponseEntity.ok(routeResult);

  }
}
