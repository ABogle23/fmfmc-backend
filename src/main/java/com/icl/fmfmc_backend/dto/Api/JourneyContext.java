package com.icl.fmfmc_backend.dto.Api;

import com.icl.fmfmc_backend.entity.enums.FallbackStrategy;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JourneyContext {
  private Boolean fallbackUsed = false;
  private List<FallbackStrategy> fallbackStrategies = new ArrayList<>();

  public void addFallbackStrategies(List<FallbackStrategy> fallbackStrategy) {
    fallbackUsed = true;
    fallbackStrategies.addAll(fallbackStrategy);
  }

  public String getFallbackMessageAsString() {
    return fallbackStrategies.stream()
        .map(FallbackStrategy::getDescription)
        .collect(Collectors.joining(", "));
  }

  public Boolean hasFallbackStrategy(FallbackStrategy strategy) {
    return fallbackStrategies.contains(strategy);
  }

}
