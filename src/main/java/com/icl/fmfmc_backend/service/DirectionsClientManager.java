package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.DirectionsClient;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DirectionsClientManager {
  private final DirectionsClient osrClient;
  private DirectionsClient activeClient;

  @Autowired
  public DirectionsClientManager(DirectionsClient osrClient) {
    this.osrClient = osrClient;
    this.activeClient = osrClient;
  }

  // supports multiple clients but only OSR is implemented atm.
  public DirectionsResponse getDirections(DirectionsRequest directionsRequest) {
    return activeClient.getDirections(directionsRequest);
//    try {
//      return activeClient.getDirections(directionsRequest);
//    } catch (Exception e) {
//      switchClient();
//      return activeClient.getDirections(directionsRequest);
//    }
  }

  private void switchClient() {
    if (activeClient.equals(osrClient)) {
      activeClient = osrClient; // TODO: add another client
    } else {
      activeClient = osrClient;
    }
  }
}
