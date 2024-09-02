package com.icl.fmfmc_backend.dto.foodEstablishment;

import org.springframework.util.MultiValueMap;

public interface FoodEstablishmentRequest {

    void setParameter(String key, String value);
    MultiValueMap<String, String> getQueryParams();

}
