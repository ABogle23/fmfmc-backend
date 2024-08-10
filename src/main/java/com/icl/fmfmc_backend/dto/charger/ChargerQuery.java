package com.icl.fmfmc_backend.dto.charger;

import com.icl.fmfmc_backend.entity.enums.AccessType;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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
    private Point topLeftLatLng;
    private Point bottomRightLatLng;

    public static class ChargerQueryBuilder {
        private GeometryFactory geometryFactory = new GeometryFactory();

        public ChargerQueryBuilder topLeftLatLng(Double lat, Double lng) {
            if (lat != null && lng != null) {
                topLeftLatLng = geometryFactory.createPoint(new Coordinate(lng, lat));
            }
            return this;
        }

        public ChargerQueryBuilder bottomRightLatLng(Double lat, Double lng) {
            if (lat != null && lng != null) {
                bottomRightLatLng = geometryFactory.createPoint(new Coordinate(lng, lat));
            }
            return this;
        }
    }
}
