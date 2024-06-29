package com.icl.fmfmc_backend.controller;


import com.icl.fmfmc_backend.service.OpenChargeMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

@RestController
@RequestMapping("/test")
public class OCMTestController {

    private final OpenChargeMapService openChargeMapService;

    public OCMTestController(OpenChargeMapService openChargeMapService) {
        this.openChargeMapService = openChargeMapService;
    }

    @GetMapping("/basic-test-api-call")
    public String basicAPICall() {
        return "API call successful!";
    }

    @GetMapping("/test-api-call")
    public String testApiCall() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        // Populate your parameters as required for the API call
//        parameters.add("exampleParam", "value");
        openChargeMapService.getChargerFromOpenChargeMapApi(parameters);
        return "API call successful!";
    }
}