package com.icl.fmfmc_backend.service;


import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/* TODO:

1 get an initial route
2 build requirements for POI in object
3 get the POIs along the route or some other search method
4 find POIs within a certain radius of a charger
5 rank POIs based on some criteria
6 return an adjacent preselected charger



*/

@Service
@RequiredArgsConstructor
@Slf4j
public class PoiService {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    private final ChargerService chargerService;
    private final FoodEstablishmentService foodEstablishmentService;
    private final GeometryService geometryService;
    private final ClusteringService clusteringService;

    public FoodEstablishment getFoodEstablishmentOnRoute(Route route, RouteRequest routeRequest) {
        logger.info("Getting food establishment");
        LineString lineString = route.getLineStringRoute();
        lineString = GeometryService.extractLineStringPortion(lineString, 0.25, 0.75);
        Polygon polygon = GeometryService.bufferLineString(lineString, 0.027); // 3km
        List<Point> chargerLocations = getChargerLocationsInPolygon(route, routeRequest, polygon);
        List<Point> clusteredChargers = ClusteringService.clusterChargers(chargerLocations, 4);

        for (Point charger : clusteredChargers) {
            System.out.println(charger.getY() + "," + charger.getX() + ",yellow,circle");
        }

        return null;
    }

    public List<Point> getChargerLocationsInPolygon(Route route, RouteRequest routeRequest, Polygon polygon) {
        logger.info("Getting chargers in polygon");
        ChargerQuery query =
                ChargerQuery.builder()
                        .polygon(polygon)
                        .connectionTypeIds(routeRequest.getConnectionTypes())
                        .minKwChargeSpeed(routeRequest.getMinKwChargeSpeed())
                        .maxKwChargeSpeed(routeRequest.getMaxKwChargeSpeed())
                        .minNoChargePoints(routeRequest.getMinNoChargePoints())
                        .accessTypeIds(routeRequest.getAccessTypes())
                        .build();

        List<Point> chargersWithinPolygon = chargerService.getChargerLocationsByParams(query);

        return chargersWithinPolygon;
    }




}
