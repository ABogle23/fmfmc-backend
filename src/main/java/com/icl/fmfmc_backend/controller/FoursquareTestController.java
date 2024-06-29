package com.icl.fmfmc_backend.controller;


import com.icl.fmfmc_backend.service.FoursquareService;
import com.icl.fmfmc_backend.service.OpenChargeMapService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class FoursquareTestController {

    private final FoursquareService foursquareService;

    public FoursquareTestController(FoursquareService foursquareService) {
        this.foursquareService = foursquareService;
    }

    @GetMapping("/fs-basic-test-api-call")
    public String basicAPICall() {
        return "API call successful!";
    }

    @GetMapping("/fs-test-api-call")
    public String testApiCall() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        // Populate your parameters as required for the API call
//        parameters.add("exampleParam", "value");
        foursquareService.getFoodEstablishmentFromFoursquarePlacesApi(parameters);
        return "API call successful!";
    }
}