package com.icl.fmfmc_backend.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The degree of deviation from the optimal route to search for chargers or food establishments")
public enum DeviationScope {
    minimal, moderate, significant, extreme
}
