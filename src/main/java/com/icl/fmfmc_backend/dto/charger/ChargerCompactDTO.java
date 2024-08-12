package com.icl.fmfmc_backend.dto.charger;

import com.icl.fmfmc_backend.entity.GeoCoordinates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChargerCompactDTO {
    private Long id;
    private GeoCoordinates geocodes;
}