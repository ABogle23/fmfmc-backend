package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequest;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequestBuilder;
import com.icl.fmfmc_backend.entity.Routing.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;

/* TODO:

1 get an initial route
2 build requirements for POI in object
3 get the POIs along the route or some other search method
4 find POIs within a certain radius of a charger
5 rank POIs based on some criteria
6 return an adjacent preselected charger

TODO: incorp chargeSpeed into restaurant scoring

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

  public Tuple2<FoodEstablishment, Charger> getFoodEstablishmentOnRoute(
      Route route, RouteRequest routeRequest) {
    logger.info("Getting food establishment");
    LineString lineString = route.getLineStringRoute();
    lineString = GeometryService.extractLineStringPortion(lineString, 0.25, 0.75);
    Polygon polygon = GeometryService.bufferLineString(lineString, 0.027); // 3km
    List<Point> chargerLocations = getChargerLocationsInPolygon(route, routeRequest, polygon);
    List<Point> clusteredChargers = ClusteringService.clusterChargers(chargerLocations, 4);

    for (Point charger : clusteredChargers) {
      System.out.println(charger.getY() + "," + charger.getX() + ",yellow,circle");
    }

    List<FoodEstablishment> foodEstablishmentsAroundClusters =
        getFoodEstablishmentsAroundClusters(routeRequest, clusteredChargers);

    List<FoodEstablishment> foodEstablishmentsInRange =
        getFoodEstablishmentsInProximityToChargers(
            chargerLocations, foodEstablishmentsAroundClusters, routeRequest);

    FoodEstablishment optimalFoodEstablishment =
        getOptimalFoodEstablishment(foodEstablishmentsInRange, routeRequest);

    Charger adjacentCharger =
        getNearestCharger(routeRequest, optimalFoodEstablishment.getLocation());

    System.out.println("Optimal Food Establishment: " + optimalFoodEstablishment);
    System.out.println("Adjacent Charger: " + adjacentCharger);

    return Tuples.of(optimalFoodEstablishment, adjacentCharger);
  }

  public List<Point> getChargerLocationsInPolygon(
      Route route, RouteRequest routeRequest, Polygon polygon) {
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

  public List<FoodEstablishment> getFoodEstablishmentsAroundClusters(
      RouteRequest routeRequest, List<Point> clusteredChargers) {

    Set<FoodEstablishment> foodEstablishments = new HashSet<>();

    for (Point cluster : clusteredChargers) {
      String[] coordinates = getLatLongAsString(cluster);
      String latLong = coordinates[0] + "," + coordinates[1];

      FoursquareRequest params =
          new FoursquareRequestBuilder()
              .setCategories(routeRequest.getEatingOptions())
              .setLl(latLong)
              .setRadius(5000)
              .createFoursquareRequest();

      List<FoodEstablishment> clusterFoodEstablishment =
          foodEstablishmentService.getFoodEstablishmentsByParam(params);

      if (clusterFoodEstablishment != null) {
        foodEstablishments.addAll(clusterFoodEstablishment);
      }
    }

    System.out.println("Total Food Establishments: " + foodEstablishments.size());
    return new ArrayList<>(foodEstablishments);
  }

  public static String[] getLatLongAsString(Point point) {
    String y = Double.toString(point.getY());
    String x = Double.toString(point.getX());
    return new String[] {y, x};
  }

  private List<FoodEstablishment> getFoodEstablishmentsInProximityToChargers(
      List<Point> clusteredChargers,
      List<FoodEstablishment> foodEstablishmentsAroundClusters,
      RouteRequest routeRequest) {

    Double maxWalkingDistance = routeRequest.getMaxWalkingDistance().doubleValue();
    System.out.println("Max walking distance: " + maxWalkingDistance);
    List<FoodEstablishment> foodEstablishmentsInRange = new ArrayList<>();

    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
      System.out.println("Name: " + foodEstablishment.getName());
      for (Point charger : clusteredChargers) {
        Double distance =
            GeometryService.calculateDistanceBetweenPoints(
                charger, foodEstablishment.getLocation());
        if (distance <= maxWalkingDistance) {
          foodEstablishmentsInRange.add(foodEstablishment);
          break;
        }
      }
    }

    return foodEstablishmentsInRange;
  }

  private FoodEstablishment getOptimalFoodEstablishment(
      List<FoodEstablishment> foodEstablishmentsInRange, RouteRequest routeRequest) {

    return foodEstablishmentsInRange.stream()
        .max(Comparator.comparingDouble(e -> calculateScore(e, routeRequest)))
        .orElse(null);
  }

  public Double calculateScore(FoodEstablishment establishment, RouteRequest routeRequest) {
    Double score = 0.0;

    score += establishment.getPopularity() != null ? establishment.getPopularity() : 0;
    score += establishment.getRating() != null ? establishment.getRating() : 0;
    score +=
        (establishment.getPrice() != null && establishment.getPrice() != 4)
            ? (double) (4 - establishment.getPrice()) / 4
            : 0;

    // TODO: add additional scoring criteria
    return score;
  }

  public Charger getNearestCharger(RouteRequest routeRequest, Point point) {
    logger.info("Getting adjacent Charger");
    ChargerQuery query =
        ChargerQuery.builder()
            .point(point)
            .radius(Double.valueOf(routeRequest.getMaxWalkingDistance()))
            .connectionTypeIds(routeRequest.getConnectionTypes())
            .minKwChargeSpeed(routeRequest.getMinKwChargeSpeed())
            .maxKwChargeSpeed(routeRequest.getMaxKwChargeSpeed())
            .minNoChargePoints(routeRequest.getMinNoChargePoints())
            .accessTypeIds(routeRequest.getAccessTypes())
            .build();

    Charger adjacentCharger = chargerService.getNearestChargerByParams(query);

    return adjacentCharger;
  }
}
