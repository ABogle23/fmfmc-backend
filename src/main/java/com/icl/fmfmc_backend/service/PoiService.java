package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequest;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequestBuilder;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import com.icl.fmfmc_backend.util.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.annotation.Retention;
import java.util.*;
import java.util.stream.Collectors;

/* TODO:

1 get an initial route
2 build requirements for POI in object
3 get the POIs along the route or some other search method
4 find POIs within a certain radius of a charger
5 rank POIs based on some criteria
6 return an adjacent preselected charger

TODO: incorp chargeSpeed into restaurant scoring
TODO: add more scoring criteria
TODO: introduced expanding search or rela


*/

@Service
@RequiredArgsConstructor
@Slf4j
public class PoiService {

  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;
  private final GeometryService geometryService;

  @Setter
  private ClusteringStrategy clusteringStrategy;

  @LogExecutionTime(message = LogMessages.RETRIEVING_FOOD_ESTABLISHMENTS)
  public Tuple2<List<FoodEstablishment>, Charger> getFoodEstablishmentOnRoute(
      Route route, RouteRequest routeRequest) {
    logger.info("Getting food establishment");

    this.setClusteringStrategy(new OutlierAdjustedKMeansClusteringService());

    LineString lineString = route.getWorkingLineStringRoute();

    // restrict search to part of route as per stoppingRange param
    Double searchStart = routeRequest.getStoppingRangeAsFraction()[0];
    Double searchEnd = routeRequest.getStoppingRangeAsFraction()[1];
    lineString = GeometryService.extractLineStringPortion(lineString, searchStart, searchEnd);

    Polygon polygon = GeometryService.bufferLineString(lineString, 0.009*6.5); // 6.5km

    // For testing
    route.setEatingOptionSearch(PolylineUtility.polygonStringToFoursquareFormat(polygon));

    List<Point> chargerLocations = getChargerLocationsInPolygon(route, routeRequest, polygon);
    List<Point> clusteredChargers = clusteringStrategy.clusterChargers(chargerLocations, 4);

    for (Point charger : clusteredChargers) {
      System.out.println(charger.getY() + "," + charger.getX() + ",yellow,circle");
    }

    List<FoodEstablishment> foodEstablishmentsAroundClusters =
        getFoodEstablishmentsAroundClusters(routeRequest, clusteredChargers);

    List<FoodEstablishment> foodEstablishmentsInRange =
        getFoodEstablishmentsInRangeOfChargers(
            chargerLocations, foodEstablishmentsAroundClusters, routeRequest);

    FoodEstablishment optimalFoodEstablishment =
        getOptimalFoodEstablishment(foodEstablishmentsInRange, routeRequest);

    Charger adjacentCharger =
        getNearestCharger(routeRequest, optimalFoodEstablishment.getLocation());

    if (routeRequest.getIncludeAlternativeEatingOptions() && adjacentCharger != null) {
      List<FoodEstablishment> subOptimalFoodEstablishments = getFoodEstablishmentsAroundAdjacentCharger(foodEstablishmentsInRange, adjacentCharger, routeRequest);
      return Tuples.of(subOptimalFoodEstablishments, adjacentCharger);
    }

    System.out.println("Optimal Food Establishment: " + optimalFoodEstablishment);
    System.out.println("Adjacent Charger: " + adjacentCharger);

    return Tuples.of(List.of(optimalFoodEstablishment), adjacentCharger);
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

    for (Point charger : chargersWithinPolygon) {
      System.out.println(charger.getY() + "," + charger.getX() + ",red,circle");
    }

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

  private List<FoodEstablishment> getFoodEstablishmentsInRangeOfChargers(
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

    System.out.println("Food Establishments in range: " + foodEstablishmentsInRange.size());

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
    score += establishment.getRating() != null ? establishment.getRating() / 10 : 0;
    score +=
        (establishment.getPrice() != null && establishment.getPrice() != 4)
            ? (double) (4 - establishment.getPrice()) / 8
            : 0;

    System.out.println("Score: " + score + " for " + establishment.getName());

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

  private List<FoodEstablishment> getFoodEstablishmentsAroundAdjacentCharger(List<FoodEstablishment> foodEstablishmentsInRange, Charger adjacentCharger, RouteRequest routeRequest) {
    List<FoodEstablishment> subOptimalFoodEstablishments = new ArrayList<>();

    for (FoodEstablishment foodEstablishment : foodEstablishmentsInRange) {
      Double distance =
          GeometryService.calculateDistanceBetweenPoints(
              adjacentCharger.getLocation(), foodEstablishment.getLocation());
      if (distance <= routeRequest.getMaxWalkingDistance()) {
        subOptimalFoodEstablishments.add(foodEstablishment);
      }
    }

    subOptimalFoodEstablishments.sort((e1, e2) -> Double.compare(
            calculateScore(e2, routeRequest), calculateScore(e1, routeRequest)));

    subOptimalFoodEstablishments =  subOptimalFoodEstablishments.stream().limit(5).collect(Collectors.toList());

    System.out.println("Suboptimal Food Establishments: " + subOptimalFoodEstablishments.size());

    return subOptimalFoodEstablishments;
  }

}
