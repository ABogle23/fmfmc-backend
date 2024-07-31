package com.icl.fmfmc_backend.integration;

import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;

public interface DirectionsClient {

  DirectionsResponse getDirections(DirectionsRequest request);
}
