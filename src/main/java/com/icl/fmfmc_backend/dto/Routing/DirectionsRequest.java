package com.icl.fmfmc_backend.dto.Routing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectionsRequest {

  private List<Double[]> coordinates;
}
