package com.icl.fmfmc_backend.config.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class FmfmcApiKeyProperties {
    @Value("${fmfmc.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
