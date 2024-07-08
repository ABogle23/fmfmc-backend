package com.icl.fmfmc_backend.dto;

import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.util.List;

@Getter
@Builder
public class ChargerQuery {
    private Polygon polygon;
    private Point point;
    private Double radius;
    private List<ConnectionType> connectionTypeIds;
    private Integer minKwChargeSpeed;
    private Integer maxKwChargeSpeed;
    private Integer minNoChargePoints;
}
