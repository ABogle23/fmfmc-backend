package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.Integration.FoursquareClient;
import com.icl.fmfmc_backend.dto.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.FoursquareRequest;
import com.icl.fmfmc_backend.entity.FoursquareRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class FoursquareTestController {

  private final FoursquareClient foursquareClient;

  public FoursquareTestController(FoursquareClient foursquareClient) {
    this.foursquareClient = foursquareClient;
  }

  @GetMapping("/fs-basic-test-api-call")
  public String basicAPICall() {
    return "API call successful!";
  }

  @GetMapping("/fs-test-api-call")
  public FoursquareResponseDTO testApiCall() {

    FoursquareRequest foursquareRequest =
        new FoursquareRequestBuilder()
            .setLl("51.472440,-0.042947")
            .setRadius(10000)
//                .setCategories(17142)
                .setMinPrice(4)
            .createFoursquareRequest();
    FoursquareResponseDTO foursquareResponseDTO = foursquareClient.getFoodEstablishmentFromFoursquarePlacesApi(foursquareRequest);
//    return "API call successful!";
    return foursquareResponseDTO;
  }
}
