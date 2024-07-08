package com.icl.fmfmc_backend.entity;

import lombok.Data;
import org.locationtech.jts.geom.LineString;

import java.util.Collections;
import java.util.List;


@Data
public class Route {

    private LineString route;

    private double distance;

    private double duration;

    private List<Charger> chargers = Collections.emptyList();

    private List<FoodEstablishment> foodEstablishments = Collections.emptyList();

    public Route(LineString route, double distance, double duration) {
        this.route = route;
        this.distance = distance;
        this.duration = duration;
    }

}
