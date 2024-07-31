package com.icl.fmfmc_backend.controller;


import com.icl.fmfmc_backend.integration.OpenChargeMapClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

@RestController
@RequestMapping("/test")
public class OCMTestController {

    private final OpenChargeMapClient openChargeMapClient;

    public OCMTestController(OpenChargeMapClient openChargeMapClient) {
        this.openChargeMapClient = openChargeMapClient;
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
        openChargeMapClient.getChargerFromOpenChargeMapApi(parameters);
        return "API call successful!";
    }
}