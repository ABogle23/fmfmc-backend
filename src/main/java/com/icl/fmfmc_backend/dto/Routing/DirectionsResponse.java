package com.icl.fmfmc_backend.dto.Routing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectionsResponse {

  private LineString lineString;
  private Double totalDuration;
  private Double totalDistance;
  private List<Double> legDistances;
  private List<Double> legDurations;
}
