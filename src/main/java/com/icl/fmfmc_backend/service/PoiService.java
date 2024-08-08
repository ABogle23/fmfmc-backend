package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.Routing.PolylineUtility;
import com.icl.fmfmc_backend.controller.JourneyController;
import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.*;
import com.icl.fmfmc_backend.entity.routing.Route;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsFoundException;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsInRangeOfChargerException;
import com.icl.fmfmc_backend.exception.service.PoiServiceException;
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

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
  private final ChargerService chargerService;
  private final FoodEstablishmentService foodEstablishmentService;
//  private final GeometryService geometryService;
  private final FoodEstablishmentBuilder requestBuilder = new FoursquareRequestBuilder();

  @Setter private ClusteringStrategy clusteringStrategy;

  @LogExecutionTime(message = LogMessages.RETRIEVING_FOOD_ESTABLISHMENTS)
  public Tuple2<List<FoodEstablishment>, Charger> getFoodEstablishmentOnRoute(Route route)
      throws NoFoodEstablishmentsFoundException,
          NoFoodEstablishmentsInRangeOfChargerException,
          PoiServiceException {
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
    route.setEatingOptionSearch(PolylineUtility.encodePolygon(polygon));

    List<Point> chargerLocations = getChargerLocationsInPolygon(route, polygon);


//    // TODO: INVESTIGATE WHY THIS IS HAPPENING
//    if (chargerLocations.isEmpty()) {
//      return Tuples.of(Collections.emptyList(), new Charger());
//    }

    List<Point> clusteredChargers = clusteringStrategy.clusterChargers(chargerLocations, 4);

    clusteredChargers = clusteringStrategy.consolidateCloseCentroids(clusteredChargers, 0.02); // 2.km

    for (Point charger : clusteredChargers) {
      System.out.println(charger.getY() + "," + charger.getX() + ",yellow,circle");
    }

    List<FoodEstablishment> foodEstablishmentsAroundClusters =
        getFoodEstablishmentsAroundClusters(route, clusteredChargers);

    // FOR TESTING
    System.out.println("Food Establishments Around Clusters Test Data");
    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
      Double X = foodEstablishment.getLocation().getX();
      Double Y = foodEstablishment.getLocation().getY();
      System.out.println("geometryFactory.createPoint(new Coordinate("+ X +", " + Y + ")),");
    }
//    System.out.println("Food Establishments Ratings Test Data");
//    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
//      Double rating = foodEstablishment.getRating();
//      System.out.println(rating + ",");
//    }
//    System.out.println("Food Establishments Popularity Test Data");
//    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
//      Double popularity = foodEstablishment.getPopularity();
//      System.out.println(popularity + ",");
//    }
//    System.out.println("Food Establishments price Test Data");
//    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
//      Integer price = foodEstablishment.getPrice();
//      System.out.println(price + ",");
//    }
    System.out.println("Charger loc Test Data");
    for (Point charger : chargerLocations) {
      Double X = charger.getX();
      Double Y = charger.getY();
      System.out.println("geometryFactory.createPoint(new Coordinate("+ X +", " + Y + ")),");
    }
    // FOR TESTING

    // if no FoodEstablishments found are in the range of a charger expand the search of the
    // chargers prior to retrying the food establishment search which involves another costly
    // api client call

    List<FoodEstablishment> foodEstablishmentsInRange = null;
    try {
      foodEstablishmentsInRange = getFoodEstablishmentsInRangeOfChargers(
          chargerLocations, foodEstablishmentsAroundClusters, route);
    } catch (NoFoodEstablishmentsInRangeOfChargerException e) {
      logger.info("Expanding charger search range prior to retrying food establishment search");
      foodEstablishmentsInRange = getFoodEstablishmentsInRangeOfChargersFallback(route, foodEstablishmentsAroundClusters);
    }

    FoodEstablishment optimalFoodEstablishment =
        getOptimalFoodEstablishment(foodEstablishmentsInRange);

    Charger adjacentCharger = getNearestCharger(route, optimalFoodEstablishment.getLocation());

    // if includeAlternativeEatingOptions is true, return the top 5 food
    // establishments around the adjacent charger
    if (route.getIncludeAlternativeEatingOptions() && adjacentCharger != null) {
      List<FoodEstablishment> subOptimalFoodEstablishments =
          getFoodEstablishmentsAroundAdjacentCharger(
              foodEstablishmentsInRange, adjacentCharger, route);
      return Tuples.of(subOptimalFoodEstablishments, adjacentCharger);
    }

    System.out.println("Optimal Food Establishment: " + optimalFoodEstablishment);
    System.out.println("Adjacent Charger: " + adjacentCharger.getId());

    // TODO: add null check for adjacent charger
    return Tuples.of(List.of(optimalFoodEstablishment), adjacentCharger);
  }

  private List<FoodEstablishment> getFoodEstablishmentsInRangeOfChargersFallback(Route route, List<FoodEstablishment> foodEstablishmentsAroundClusters) throws NoFoodEstablishmentsInRangeOfChargerException {
    List<FoodEstablishment> foodEstablishmentsInRange = new ArrayList<>();
    List<Point> chargerLocations = new ArrayList<>();
    List<Point> foodEstablishmentLocations = new ArrayList<>();
    for (FoodEstablishment foodEstablishment : foodEstablishmentsAroundClusters) {
        foodEstablishmentLocations.add(foodEstablishment.getLocation());
    }
    Polygon expandedPolygon =
              GeometryService.getBufferedMinimumBoundingCircleAsPolygon(foodEstablishmentLocations,0.0045); // 500m is 0.0045
    chargerLocations = getChargerLocationsInPolygon(route, expandedPolygon);
    foodEstablishmentsInRange = getFoodEstablishmentsInRangeOfChargers(
            chargerLocations, foodEstablishmentsAroundClusters, route);
    return foodEstablishmentsInRange;
  }

  private List<Point> getChargerLocationsInPolygon(Route route, Polygon polygon) {
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

  private List<FoodEstablishment> getFoodEstablishmentsAroundClusters(
      Route route, List<Point> clusteredChargers)
      throws NoFoodEstablishmentsFoundException, PoiServiceException {
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

    // FOR TESTING
    route.setEatingSearchCircles(new ArrayList<>());
    List<Polygon> searchPolygons = new ArrayList<>();
    // FOR TESTING

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
              .setMinPrice(route.getMinPrice())
              .setMaxPrice(route.getMaxPrice())
              .build();

      // FOR TESTING
      searchPolygons.add(GeometryService.createCircle(cluster, searchRadius));
      // FOR TESTING


      List<FoodEstablishment> clusterFoodEstablishments =
              null;
      try {
        clusterFoodEstablishments = foodEstablishmentService.getFoodEstablishmentsByParam(params);
      } catch (Exception e) {
        logger.error("Error occurred while fetching food establishments: " + e.getMessage());
        throw new PoiServiceException("Error occurred while fetching food establishments: " + e.getMessage());
      }

      if (clusterFoodEstablishments != null) {
        for (FoodEstablishment fe : clusterFoodEstablishments) {
          String id = fe.getId();
          if (!foodEstablishments.containsKey(id)) {
            foodEstablishments.put(id, fe);
          }
        }
      }
    }

    // FOR TESTING
    route.setEatingSearchCircles(searchPolygons);
    // FOR TESTING

    if (foodEstablishments.isEmpty()) {
      logger.error("No food establishments returned from API call");
      throw new NoFoodEstablishmentsFoundException();
    }

    System.out.println("Total Food Establishments: " + foodEstablishments.size());
    return new ArrayList<>(foodEstablishments.values());
  }

  private static String[] getLatLongAsString(Point point) {
    String y = Double.toString(point.getY());
    String x = Double.toString(point.getX());
    return new String[] {y, x};
  }

  private List<FoodEstablishment> getFoodEstablishmentsInRangeOfChargers(
      List<Point> clusteredChargers,
      List<FoodEstablishment> foodEstablishmentsAroundClusters,
      Route route) throws NoFoodEstablishmentsInRangeOfChargerException {

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

    if (foodEstablishmentsInRange.isEmpty()) {
      logger.error("No food establishments found within range of charger");
      throw new NoFoodEstablishmentsInRangeOfChargerException();
    }

    return foodEstablishmentsInRange;
  }

  private FoodEstablishment getOptimalFoodEstablishment(
      List<FoodEstablishment> foodEstablishmentsInRange) {

    return foodEstablishmentsInRange.stream()
        .max(Comparator.comparingDouble(e -> calculateScore(e)))
        .orElse(null);
  }

  private Double calculateScore(FoodEstablishment establishment) {
    Double score = 0.0;

    score += establishment.getPopularity() != null ? establishment.getPopularity() : 0.5;
    score += establishment.getRating() != null ? (establishment.getRating() / 10) : 0.5;
    score +=
        (establishment.getPrice() != null && establishment.getPrice() != 4)
            ?  ((4.0 - establishment.getPrice()) / 20.0)
            : 0;

    System.out.println("Score: " + score + " for " + establishment.getName());

    return score;
  }

  private Charger getNearestCharger(Route route, Point point) {
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
