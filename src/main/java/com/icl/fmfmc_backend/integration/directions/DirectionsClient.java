package com.icl.fmfmc_backend.integration.directions;

import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;

public interface DirectionsClient {

  /**
   * Retrieves directions based on the provided request.
   *
   * @param request the DirectionsRequest object containing the request details
   * @return a DirectionsResponse object containing the directions
   */
  DirectionsResponse getDirections(DirectionsRequest request);
}
