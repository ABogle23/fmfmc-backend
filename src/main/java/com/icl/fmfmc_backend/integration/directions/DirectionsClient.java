package com.icl.fmfmc_backend.integration.directions;

import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;

public interface DirectionsClient {

  DirectionsResponse getDirections(DirectionsRequest request);
}
