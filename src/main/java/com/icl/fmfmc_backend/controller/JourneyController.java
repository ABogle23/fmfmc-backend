package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.dto.api.ApiResponse;
import com.icl.fmfmc_backend.dto.api.JourneyContext;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.service.JourneyService;

import com.icl.fmfmc_backend.dto.api.RouteRequest;
import com.icl.fmfmc_backend.dto.api.RouteResult;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JourneyController {

  private final JourneyService journeyService;

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);

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
