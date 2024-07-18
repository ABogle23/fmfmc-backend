package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.controller.RouteController;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.*;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
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
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.annotation.Retention;
import java.util.*;
import java.util.stream.Collectors;

/*

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
  private final FoodEstablishmentBuilder requestBuilder = new FoursquareRequestBuilder();

  @Setter private ClusteringStrategy clusteringStrategy;

  @LogExecutionTime(message = LogMessages.RETRIEVING_FOOD_ESTABLISHMENTS)
  public Tuple2<List<FoodEstablishment>, Charger> getFoodEstablishmentOnRoute(Route route) {
    logger.info("Getting food establishment");

    this.setClusteringStrategy(new OutlierAdjustedKMeansClusteringService());

    LineString lineString = route.getWorkingLineStringRoute();

    // restrict search to part of route as per stoppingRange param
    Double searchStart = route.getStoppingRangeAsFraction()[0];
    Double searchEnd = route.getStoppingRangeAsFraction()[1];
    lineString = GeometryService.extractLineStringPortion(lineString, searchStart, searchEnd);

    Double foodEstablishmentDeviationScope = route.getEatingOptionSearchDeviationAsFraction(); // km

    Polygon polygon =
        GeometryService.bufferLineString(lineString, 0.009 * foodEstablishmentDeviationScope);

    // For testing
    route.setEatingOptionSearch(PolylineUtility.polygonStringToFoursquareFormat(polygon));

    List<Point> chargerLocations = getChargerLocationsInPolygon(route, polygon);
    List<Point> clusteredChargers = clusteringStrategy.clusterChargers(chargerLocations, 4);

    for (Point charger : clusteredChargers) {
      System.out.println(charger.getY() + "," + charger.getX() + ",yellow,circle");
    }

    List<FoodEstablishment> foodEstablishmentsAroundClusters =
        getFoodEstablishmentsAroundClusters(route, clusteredChargers);

    List<FoodEstablishment> foodEstablishmentsInRange =
        getFoodEstablishmentsInRangeOfChargers(
            chargerLocations, foodEstablishmentsAroundClusters, route);

    // needs fallback strategy
    FoodEstablishment optimalFoodEstablishment =
        getOptimalFoodEstablishment(foodEstablishmentsInRange);

    if (optimalFoodEstablishment == null) {
      return Tuples.of(Collections.emptyList(), null);
    }

    Charger adjacentCharger = getNearestCharger(route, optimalFoodEstablishment.getLocation());

    // if includeAlternativeEatingOptions is true, return the top 5 food establishments around the
    // adjacent charger
    if (route.getIncludeAlternativeEatingOptions() && adjacentCharger != null) {
      List<FoodEstablishment> subOptimalFoodEstablishments =
          getFoodEstablishmentsAroundAdjacentCharger(
              foodEstablishmentsInRange, adjacentCharger, route);
      return Tuples.of(subOptimalFoodEstablishments, adjacentCharger);
    }

    System.out.println("Optimal Food Establishment: " + optimalFoodEstablishment);
    System.out.println("Adjacent Charger: " + adjacentCharger);

    return Tuples.of(List.of(optimalFoodEstablishment), adjacentCharger);
  }

  public List<Point> getChargerLocationsInPolygon(Route route, Polygon polygon) {
    logger.info("Getting chargers in polygon");
    ChargerQuery query =
        ChargerQuery.builder()
            .polygon(polygon)
            .connectionTypeIds(route.getConnectionTypes())
            .minKwChargeSpeed(route.getMinKwChargeSpeed())
            .maxKwChargeSpeed(route.getMaxKwChargeSpeed())
            .minNoChargePoints(route.getMinNoChargePoints())
            .accessTypeIds(route.getAccessTypes())
            .build();

    List<Point> chargersWithinPolygon = chargerService.getChargerLocationsByParams(query);

    for (Point charger : chargersWithinPolygon) {
      System.out.println(charger.getY() + "," + charger.getX() + ",red,circle");
    }

    return chargersWithinPolygon;
  }

  public List<FoodEstablishment> getFoodEstablishmentsAroundClusters(
      Route route, List<Point> clusteredChargers) {
    // TODO: get rid of this and rename the func it calls
    Integer searchRadius =
        switch (route.getEatingOptionSearchDeviation()) {
          case minimal -> 4500;
          case moderate -> 5000;
          case significant -> 10000;
          case extreme -> 15000;
          default -> 5000;
        };

    Map<String, FoodEstablishment> foodEstablishments = new HashMap<>();

    for (Point cluster : clusteredChargers) {
      String[] coordinates = getLatLongAsString(cluster);
      //      String latLong = coordinates[0] + "," + coordinates[1];
      String latitude = coordinates[0];
      String longitude = coordinates[1];

      FoodEstablishmentRequest params =
          requestBuilder
              .setCategories(route.getEatingOptions())
              .setLatitude(latitude)
              .setLongitude(longitude)
              .setRadius(searchRadius)
              .build();

      List<FoodEstablishment> clusterFoodEstablishments =
          foodEstablishmentService.getFoodEstablishmentsByParam(params);

      if (clusterFoodEstablishments != null) {
        for (FoodEstablishment fe : clusterFoodEstablishments) {
          String id = fe.getId();
          if (!foodEstablishments.containsKey(id)) {
            foodEstablishments.put(id, fe);
          }
        }
      }
    }

    System.out.println("Total Food Establishments: " + foodEstablishments.size());
    return new ArrayList<>(foodEstablishments.values());
  }

  public static String[] getLatLongAsString(Point point) {
    String y = Double.toString(point.getY());
    String x = Double.toString(point.getX());
    return new String[] {y, x};
  }

  private List<FoodEstablishment> getFoodEstablishmentsInRangeOfChargers(
      List<Point> clusteredChargers,
      List<FoodEstablishment> foodEstablishmentsAroundClusters,
      Route route) {

    Double maxWalkingDistance = route.getMaxWalkingDistance().doubleValue();
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
      List<FoodEstablishment> foodEstablishmentsInRange) {

    return foodEstablishmentsInRange.stream()
        .max(Comparator.comparingDouble(e -> calculateScore(e)))
        .orElse(null);
  }

  public Double calculateScore(FoodEstablishment establishment) {
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

  public Charger getNearestCharger(Route route, Point point) {
    logger.info("Getting adjacent Charger");
    ChargerQuery query =
        ChargerQuery.builder()
            .point(point)
            .radius(Double.valueOf(route.getMaxWalkingDistance()))
            .connectionTypeIds(route.getConnectionTypes())
            .minKwChargeSpeed(route.getMinKwChargeSpeed())
            .maxKwChargeSpeed(route.getMaxKwChargeSpeed())
            .minNoChargePoints(route.getMinNoChargePoints())
            .accessTypeIds(route.getAccessTypes())
            .build();

    Charger adjacentCharger = chargerService.getNearestChargerByParams(query);

    return adjacentCharger;
  }

  private List<FoodEstablishment> getFoodEstablishmentsAroundAdjacentCharger(
      List<FoodEstablishment> foodEstablishmentsInRange, Charger adjacentCharger, Route route) {
    List<FoodEstablishment> subOptimalFoodEstablishments = new ArrayList<>();

    for (FoodEstablishment foodEstablishment : foodEstablishmentsInRange) {
      Double distance =
          GeometryService.calculateDistanceBetweenPoints(
              adjacentCharger.getLocation(), foodEstablishment.getLocation());
      if (distance <= route.getMaxWalkingDistance()) {
        subOptimalFoodEstablishments.add(foodEstablishment);
      }
    }

    subOptimalFoodEstablishments.sort(
        (e1, e2) -> Double.compare(calculateScore(e2), calculateScore(e1)));

    subOptimalFoodEstablishments =
        subOptimalFoodEstablishments.stream().limit(5).collect(Collectors.toList());

    System.out.println("Suboptimal Food Establishments: " + subOptimalFoodEstablishments.size());

    return subOptimalFoodEstablishments;
  }
}
