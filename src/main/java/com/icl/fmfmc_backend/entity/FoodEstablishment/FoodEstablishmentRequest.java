package com.icl.fmfmc_backend.entity.FoodEstablishment;

import org.springframework.util.MultiValueMap;

public interface FoodEstablishmentRequest {

    void setParameter(String key, String value);
    MultiValueMap<String, String> getQueryParams();

}
