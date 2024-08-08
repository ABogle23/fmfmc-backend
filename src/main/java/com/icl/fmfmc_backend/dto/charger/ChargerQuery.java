package com.icl.fmfmc_backend.dto.charger;

import com.icl.fmfmc_backend.entity.enums.AccessType;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import lombok.Builder;
import lombok.Getter;
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
    private List<AccessType> accessTypeIds;
    private Integer minKwChargeSpeed;
    private Integer maxKwChargeSpeed;
    private Integer minNoChargePoints;
}
