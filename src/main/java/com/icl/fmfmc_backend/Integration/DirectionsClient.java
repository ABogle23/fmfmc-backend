package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.dto.Routing.OSRDirectionsServiceGeoJSONRequest;

import java.util.List;

public interface DirectionsClient {

  DirectionsResponse getDirections(DirectionsRequest request);
}
