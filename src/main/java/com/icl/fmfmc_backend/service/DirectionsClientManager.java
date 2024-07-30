package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.DirectionsClient;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.exception.BadRequestException;
import com.icl.fmfmc_backend.exception.DirectionsClientException;
import com.icl.fmfmc_backend.exception.ServiceUnavailableException;
import com.icl.fmfmc_backend.exception.JourneyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class DirectionsClientManager {

  private static final Logger logger = LoggerFactory.getLogger(DirectionsClientManager.class);
  private final DirectionsClient osrClient;
  private final DirectionsClient mapboxClient;
  private DirectionsClient activeClient;
  private LocalDateTime switchTime = null;
  private static final long SWITCH_BACK_AFTER_HOURS = 1;

  @Autowired
  public DirectionsClientManager(
      @Qualifier("osrClient") DirectionsClient osrClient,
      @Qualifier("mapboxClient") DirectionsClient mapboxClient) {
    this.osrClient = osrClient;
    this.mapboxClient = mapboxClient;
    this.activeClient = osrClient;
  }

  // supports multiple clients but only OSR is implemented atm.
  //  public DirectionsResponse getDirections(DirectionsRequest directionsRequest) {
  //    try {
  //      return activeClient.getDirections(directionsRequest);
  //    } catch (Exception e) {
  //        logger.error("Error occurred while fetching directions from active client: {}",
  // e.getMessage());
  //        logger.info("Switching to other client...");
  //      switchClient();
  //      return activeClient.getDirections(directionsRequest);
  //    }
  //  }

  public DirectionsResponse getDirections(DirectionsRequest directionsRequest)
      throws DirectionsClientException {
    if (shouldSwitchBackToOsr()) {
      switchClient();
    }
    try {
      return activeClient.getDirections(directionsRequest);
    } catch (ServiceUnavailableException e) {
      logger.error(
          "ServiceUnavailableException occurred while fetching directions from active client: {}",
          e.getMessage());
      logger.info("Switching to other client...");
      switchClient();
      return activeClient.getDirections(directionsRequest);
    } catch (BadRequestException e) {
      logger.error(
          "DirectionsClientException occurred while fetching directions from active client: {}", e.getMessage());
      throw new DirectionsClientException(
          "Error occurred while fetching directions from active client: " + e.getMessage());
    } catch (Exception e) {
      logger.error(
              "RuntimeException occurred while fetching directions from active client: {}", e.getMessage());
      throw new RuntimeException();
    }
  }

  private void switchClient() {
    if (activeClient.equals(osrClient)) {
      activeClient = mapboxClient;
      logger.info("Switched to Mapbox client");
      switchTime = LocalDateTime.now();
    } else {
      activeClient = osrClient;
      logger.info("Switched to OSR client");
      switchTime = null;
    }
  }

  public void setClient(String client) {
    if (client.equals("osr")) {
      activeClient = osrClient;
      logger.info("Switched to OSR client");
    } else if (client.equals("mapbox")) {
      activeClient = mapboxClient;
      logger.info("Switched to Mapbox client");
    } else {
      logger.error("Invalid client name: {}", client);
    }
  }

  private boolean shouldSwitchBackToOsr() {
    if (switchTime == null) {
      return false;
    }
    long hoursSinceSwitch = ChronoUnit.HOURS.between(switchTime, LocalDateTime.now());
    return hoursSinceSwitch >= SWITCH_BACK_AFTER_HOURS;
  }

  public String getActiveClientName() {
    if (activeClient == osrClient) {
      return "osr";
    } else if (activeClient == mapboxClient) {
      return "mapbox";
    } else {
      return "unknown";
    }
  }

}
