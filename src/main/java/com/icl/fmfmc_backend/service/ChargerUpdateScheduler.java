package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.OpenChargeMapClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerUpdateScheduler {

  private final ChargerService chargerService;
  private final OpenChargeMapClient openChargeMapClient;
  private final Logger logger = LoggerFactory.getLogger(ChargerUpdateScheduler.class);

  @Scheduled(cron = "0 0 1 * * ?", zone = "Europe/London") // update every day at 01:00
  public void updateChargers() {
    logger.info("Charger update started");
    List<MultiValueMap<String, String>> parameterList = createParameterListForDifferentRegions();
    parameterList.forEach(
        params -> {
          openChargeMapClient.getChargerFromOpenChargeMapApi(params);
        });

    logger.info("Charger update completed successfully");
  }

  private List<MultiValueMap<String, String>> createParameterListForDifferentRegions() {
    List<MultiValueMap<String, String>> parametersList = new ArrayList<>();

    // each string represents a bounding box for a specific UK region
    String[] boundingBoxes =
        new String[] {
          "(59.64072,-9.36687),(55.20698,1.69141)",
          "(55.25771,-5.73908),(53.36158,1.69141)",
          "(53.40233,-5.73908),(51.88758,2.65172)",
          "(51.95552,-5.62106),(51.49268,2.140601)",
          "(51.50721,-5.62106),(51.18418,2.14060)",
          "(51.20671,-6.06923),(49.868912,2.14060)"
        };

    for (String bbox : boundingBoxes) {
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("boundingbox", bbox);
      parametersList.add(params);
    }

    return parametersList;
  }
}
