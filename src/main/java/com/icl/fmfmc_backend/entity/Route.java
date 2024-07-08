package com.icl.fmfmc_backend.entity;

import lombok.Data;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.Collections;
import java.util.List;


@Data
public class Route {

    private LineString lineStringRoute;

    private Polygon bufferedLineString;

    private double distance;

    private double duration;

    private List<Charger> chargers = Collections.emptyList();

    private List<FoodEstablishment> foodEstablishments = Collections.emptyList();

    public Route(LineString route, Polygon bufferedLineString, double distance, double duration) {
        this.lineStringRoute = route;
        this.bufferedLineString = bufferedLineString;
        this.distance = distance;
        this.duration = duration;
    }

}
