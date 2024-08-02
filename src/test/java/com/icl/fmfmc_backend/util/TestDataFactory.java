package com.icl.fmfmc_backend.util;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.entity.AddressInfo;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Charger.Connection;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.GeoCoordinates;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import com.icl.fmfmc_backend.entity.enums.StoppingRange;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestDataFactory {
  private static final GeometryFactory geometryFactory = new GeometryFactory();

  public static RouteRequest createDefaultRouteRequest() {
    RouteRequest request = new RouteRequest();
    request.setStartLat(51.110251);
    request.setStartLong(-2.109733);
    request.setEndLat(51.108095);
    request.setEndLong(-2.091537);
    request.setElectricVehicleId(null);
    request.setStartingBattery(0.9);
    request.setEvRange(100000.0);
    request.setBatteryCapacity(60.0);
    request.setMinChargeLevel(0.2);
    request.setChargeLevelAfterEachStop(0.9);
    request.setFinalDestinationChargeLevel(0.2);
    request.setConnectionTypes(null);
    request.setAccessTypes(null);
    request.setMinKwChargeSpeed(3);
    request.setMaxKwChargeSpeed(null);
    request.setStopForEating(false);
    request.setEatingOptions(List.of("restaurant", "cafe", "fastFood"));
    request.setMinPrice(null);
    request.setMaxPrice(null);
    request.setMaxWalkingDistance(500);
    request.setIncludeAlternativeEatingOptions(false);
    request.setStoppingRange(StoppingRange.middle);
    request.setChargerSearchDeviation(DeviationScope.minimal);
    request.setEatingOptionSearchDeviation(DeviationScope.minimal);

    return request;
  }

  public static RouteRequest createCustomRouteRequest(
      double startLat, double startLong, double endLat, double endLong) {
    RouteRequest request = new RouteRequest();
    request.setStartLat(startLat);
    request.setStartLong(startLong);
    request.setEndLat(endLat);
    request.setEndLong(endLong);
    // set additional properties based on parameters
    return request;
  }

  public static DirectionsResponse createDefaultDirectionsResponse() {
    DirectionsResponse directionsResponse = new DirectionsResponse();

    LineString linestring = GeometryUtil.createLineString(TestGeoConstants.ls1);

    directionsResponse.setLineString(linestring);
    directionsResponse.setTotalDuration(TestGeoConstants.ls1TotalDuration);
    directionsResponse.setTotalDistance(TestGeoConstants.ls1TotalDistance);
    directionsResponse.setLegDurations(TestGeoConstants.ls1legDurations);
    directionsResponse.setLegDistances(TestGeoConstants.ls1legDistances);


    return directionsResponse;
  }

  public static DirectionsResponse createSnappedDirectionsResponse() {
    DirectionsResponse directionsResponse = new DirectionsResponse();

    LineString linestring = GeometryUtil.createLineString(TestGeoConstants.ls1Snapped);

    directionsResponse.setLineString(linestring);
    directionsResponse.setTotalDuration(TestGeoConstants.ls1SnappedTotalDuration);
    directionsResponse.setTotalDistance(TestGeoConstants.ls1SnappedTotalDistance);
    directionsResponse.setLegDurations(TestGeoConstants.ls1SnappedLegDurations);
    directionsResponse.setLegDistances(TestGeoConstants.ls1SnappedLegDistances);


    return directionsResponse;
  }

  public static DirectionsResponse createFinalDirectionsResponse() {
    DirectionsResponse directionsResponse = new DirectionsResponse();

    LineString linestring = GeometryUtil.createLineString(TestGeoConstants.ls1Final);

    directionsResponse.setLineString(linestring);
    directionsResponse.setTotalDuration(TestGeoConstants.ls1FinalTotalDuration);
    directionsResponse.setTotalDistance(TestGeoConstants.ls1FinalTotalDistance);
    directionsResponse.setLegDurations(TestGeoConstants.ls1FinalLegDurations);
    directionsResponse.setLegDistances(TestGeoConstants.ls1FinalLegDistances);


    return directionsResponse;
  }




  public static Route createDefaultRoute() {
    Route route = new Route(createDefaultDirectionsResponse(), createDefaultRouteRequest());

    return route;
  }

  public static FoodEstablishment createDefaultFoodEstablishment(
          String establishmentId, String name, Double x, Double y) {
    FoodEstablishment foodEstablishment = new FoodEstablishment();
    foodEstablishment.setId(establishmentId);
    foodEstablishment.setName(name);
//    foodEstablishment.setCategories(Arrays.asList(FoodCategory.RESTAURANT, FoodCategory.CAFE)); // Assuming Category is an enum
    foodEstablishment.setGeocodes(new GeoCoordinates(x, y)); // Assuming GeoCoordinates stores coordinates directly
    foodEstablishment.setLocation(geometryFactory.createPoint(new Coordinate(x, y)));
    foodEstablishment.setClosedStatus("Open");
    foodEstablishment.setPopularity(0.5);
    foodEstablishment.setPrice(2); // Assuming price is an integer level 1-3
    foodEstablishment.setRating(5.0);
    foodEstablishment.setCreatedAt(LocalDateTime.now());
    foodEstablishment.setWebsite("http://www.example.com");

    return foodEstablishment;
  }

  public static Charger createDefaultCharger(
      Long chargerId, Integer numberOfConnections, Double x, Double y) {
    Charger charger = new Charger();
    charger.setId(chargerId);
    charger.setNumberOfPoints(Long.valueOf(numberOfConnections));
    charger.setStatusTypeID(50L);
    charger.setUsageTypeID(25L);
    charger.setLocation(geometryFactory.createPoint(new Coordinate(x, y)));

    List<Connection> connections = new ArrayList<>();
    for (int i = 0; i < numberOfConnections; i++) {
      Connection connection = new Connection();
      connection.setId(Long.valueOf(i + 1));
      connection.setConnectionTypeID(25L);
      connection.setStatusTypeID(50L);
      connection.setPowerKW(50L);
      connections.add(connection);
    }

    charger.setConnections(connections);
    return charger;
  }

  public static List<Charger> createChargersForBatteryTest() {

    Charger charger1 = new Charger();
    charger1.setId(41030L);
    charger1.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.42455459999996, 51.2901275)));

    Charger charger2 = new Charger();
    charger2.setId(72601L);
    charger2.setLocation(geometryFactory.createPoint(new Coordinate(-1.607693, 51.205902)));

    Charger charger3 = new Charger();
    charger3.setId(75045L);
    charger3.setLocation(geometryFactory.createPoint(new Coordinate(-1.206437, 51.466953)));

    Charger charger4 = new Charger();
    charger4.setId(90205L);
    charger4.setLocation(geometryFactory.createPoint(new Coordinate(-1.329515, 51.424781)));

    Charger charger5 = new Charger();
    charger5.setId(90470L);
    charger5.setLocation(geometryFactory.createPoint(new Coordinate(-1.328098, 51.42374)));

    Charger charger6 = new Charger();
    charger6.setId(109188L);
    charger6.setLocation(geometryFactory.createPoint(new Coordinate(-1.89469, 51.164319)));

    Charger charger7 = new Charger();
    charger7.setId(112482L);
    charger7.setLocation(geometryFactory.createPoint(new Coordinate(-1.535015, 51.213795)));

    Charger charger8 = new Charger();
    charger8.setId(117934L);
    charger8.setLocation(geometryFactory.createPoint(new Coordinate(-1.5354433, 51.214881)));

    Charger charger9 = new Charger();
    charger9.setId(128366L);
    charger9.setLocation(geometryFactory.createPoint(new Coordinate(-1.7555, 51.177601)));

    Charger charger10 = new Charger();
    charger10.setId(149301L);
    charger10.setLocation(geometryFactory.createPoint(new Coordinate(-1.766382, 51.175978)));

    Charger charger11 = new Charger();
    charger11.setId(156485L);
    charger11.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.75714171113339, 51.1770568092747)));

    Charger charger12 = new Charger();
    charger12.setId(168555L);
    charger12.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.756329629088782, 51.177222921626964)));

    Charger charger13 = new Charger();
    charger13.setId(174174L);
    charger13.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.77905423119387, 51.1738926219467)));

    Charger charger14 = new Charger();
    charger14.setId(188518L);
    charger14.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.75839094040271, 51.1770625711239)));

    Charger charger15 = new Charger();
    charger15.setId(188519L);
    charger15.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.75903966021008, 51.1770575784368)));

    Charger charger16 = new Charger();
    charger16.setId(212484L);
    charger16.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.76038439417871, 51.1771454290478)));

    Charger charger17 = new Charger();
    charger17.setId(258995L);
    charger17.setLocation(geometryFactory.createPoint(new Coordinate(-1.31031, 51.448921)));

    Charger charger18 = new Charger();
    charger18.setId(272492L);
    charger18.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.7792164864164874, 51.172256412234844)));

    Charger charger19 = new Charger();
    charger19.setId(279154L);
    charger19.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.6678794663969492, 51.19135722554175)));

    Charger charger20 = new Charger();
    charger20.setId(285685L);
    charger20.setLocation(geometryFactory.createPoint(new Coordinate(-1.281038, 51.453228)));

    // Add chargers to a collection or use them as needed

    Connection connection1 = new Connection();
    connection1.setPowerKW(50L);
    charger1.setConnections(List.of(connection1));

    Connection connection2 = new Connection();
    connection2.setPowerKW(50L);
    charger2.setConnections(List.of(connection2));

    Connection connection3 = new Connection();
    connection3.setPowerKW(50L);
    charger3.setConnections(List.of(connection3));

    Connection connection4 = new Connection();
    connection4.setPowerKW(50L);
    charger4.setConnections(List.of(connection4));

    Connection connection5 = new Connection();
    connection5.setPowerKW(50L);
    charger5.setConnections(List.of(connection5));

    Connection connection6 = new Connection();
    connection6.setPowerKW(50L);
    charger6.setConnections(List.of(connection6));

    Connection connection7 = new Connection();
    connection7.setPowerKW(50L);
    charger7.setConnections(List.of(connection7));

    Connection connection8 = new Connection();
    connection8.setPowerKW(50L);
    charger8.setConnections(List.of(connection8));

    Connection connection9 = new Connection();
    connection9.setPowerKW(50L);
    charger9.setConnections(List.of(connection9));

    Connection connection10 = new Connection();
    connection10.setPowerKW(50L);
    charger10.setConnections(List.of(connection10));

    Connection connection11 = new Connection();
    connection11.setPowerKW(50L);
    charger11.setConnections(List.of(connection11));

    Connection connection12 = new Connection();
    connection12.setPowerKW(50L);
    charger12.setConnections(List.of(connection12));

    Connection connection13 = new Connection();
    connection13.setPowerKW(50L);
    charger13.setConnections(List.of(connection13));

    Connection connection14 = new Connection();
    connection14.setPowerKW(50L);
    charger14.setConnections(List.of(connection14));

    Connection connection15 = new Connection();
    connection15.setPowerKW(50L);
    charger15.setConnections(List.of(connection15));

    Connection connection16 = new Connection();
    connection16.setPowerKW(50L);
    charger16.setConnections(List.of(connection16));

    Connection connection17 = new Connection();
    connection17.setPowerKW(50L);
    charger17.setConnections(List.of(connection17));

    Connection connection18 = new Connection();
    connection18.setPowerKW(50L);
    charger18.setConnections(List.of(connection18));

    Connection connection19 = new Connection();
    connection19.setPowerKW(50L);
    charger19.setConnections(List.of(connection19));

    Connection connection20 = new Connection();
    connection20.setPowerKW(50L);
    charger20.setConnections(List.of(connection20));

    List<Charger> chargers =
        List.of(
            charger1, charger2, charger3, charger4, charger5, charger6, charger7, charger8,
            charger9, charger10, charger11, charger12, charger13, charger14, charger15, charger16,
            charger17, charger18, charger19, charger20);

    return chargers;
  }

  public static List<Charger> createChargersForCurrentBatterySetTest() {

    Charger charger12 = new Charger();
    charger12.setId(168555L);
    charger12.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.756329629088782, 51.177222921626964)));

    Charger charger16 = new Charger();
    charger16.setId(212484L);
    charger16.setLocation(
        geometryFactory.createPoint(new Coordinate(-1.76038439417871, 51.1771454290478)));

    Charger charger17 = new Charger();
    charger17.setId(258995L);
    charger17.setLocation(geometryFactory.createPoint(new Coordinate(-1.31031, 51.448921)));

    // Add chargers to a collection or use them as needed

    Connection connection12 = new Connection();
    connection12.setPowerKW(50L);
    charger12.setConnections(List.of(connection12));

    Connection connection16 = new Connection();
    connection16.setPowerKW(50L);
    charger16.setConnections(List.of(connection16));

    Connection connection17 = new Connection();
    connection17.setPowerKW(50L);
    charger17.setConnections(List.of(connection17));

    List<Charger> chargers = List.of(charger12, charger16, charger17);

    return chargers;
  }

  // Charger ID: 109188, Distance: 16974.848589028865 m
  // Charger ID: 174174, Distance: 25412.367862517887 m
  // Charger ID: 272492, Distance: 25412.367862517887 m
  // Charger ID: 149301, Distance: 26474.045876270982 m
  // Charger ID: 212484, Distance: 26797.137389093336 m
  // Charger ID: 188519, Distance: 26876.037800249553 m
  // Charger ID: 128366, Distance: 27012.34666117306 m
  // Charger ID: 156485, Distance: 27012.34666117306 m
  // Charger ID: 168555, Distance: 27012.34666117306 m
  // Charger ID: 188518, Distance: 27012.34666117306 m
  // Charger ID: 279154, Distance: 33545.94457566622 m
  // Charger ID: 72601, Distance: 38028.64500170254 m
  // Charger ID: 112482, Distance: 44081.69993378169 m
  // Charger ID: 117934, Distance: 44081.69993378169 m
  // Charger ID: 41030, Distance: 55586.058695772845 m
  // Charger ID: 90205, Distance: 73645.62987574692 m
  // Charger ID: 90470, Distance: 73645.62987574692 m
  // Charger ID: 258995, Distance: 76559.66523581218 m
  // Charger ID: 285685, Distance: 78643.63330601106 m
  // Charger ID: 75045, Distance: 84383.18199108054 m

  // 90986.9922907895

  public static List<Point> createPointsForFoodEstablishments() {

    List<Point> foodEstablishmentPoints =
        List.of(
            geometryFactory.createPoint(new Coordinate(-1.781991, 51.172076)),
            geometryFactory.createPoint(new Coordinate(-1.638145, 51.238996)),
            geometryFactory.createPoint(new Coordinate(-1.781991, 51.172076)),
            geometryFactory.createPoint(new Coordinate(-1.568989, 51.212772)),
            geometryFactory.createPoint(new Coordinate(-1.478435, 51.20631)),
            geometryFactory.createPoint(new Coordinate(-1.547182, 51.218065)),
            geometryFactory.createPoint(new Coordinate(-1.396735, 51.252272)),
            geometryFactory.createPoint(new Coordinate(-1.739659, 51.188802)),
            geometryFactory.createPoint(new Coordinate(-1.779837, 51.178392)),
            geometryFactory.createPoint(new Coordinate(-1.760909, 51.190831)),
            geometryFactory.createPoint(new Coordinate(-1.401874, 51.253664)),
            geometryFactory.createPoint(new Coordinate(-1.757691, 51.177134)),
            geometryFactory.createPoint(new Coordinate(-1.478755, 51.209076)),
            geometryFactory.createPoint(new Coordinate(-1.756643, 51.177527)),
            geometryFactory.createPoint(new Coordinate(-1.476685, 51.214551)),
            geometryFactory.createPoint(new Coordinate(-1.497799, 51.219473)),
            geometryFactory.createPoint(new Coordinate(-1.661062, 51.234332)),
            geometryFactory.createPoint(new Coordinate(-1.48247, 51.209293)),
            geometryFactory.createPoint(new Coordinate(-1.511431, 51.209815)),
            geometryFactory.createPoint(new Coordinate(-1.758065, 51.177439)),
            geometryFactory.createPoint(new Coordinate(-1.780596, 51.178345)),
            geometryFactory.createPoint(new Coordinate(-1.759346, 51.174406)),
            geometryFactory.createPoint(new Coordinate(-1.600511, 51.219186)),
            geometryFactory.createPoint(new Coordinate(-1.486283, 51.294677)),
            geometryFactory.createPoint(new Coordinate(-1.772125, 51.1984)),
            geometryFactory.createPoint(new Coordinate(-1.494722, 51.203761)),
            geometryFactory.createPoint(new Coordinate(-1.520488, 51.206326)),
            geometryFactory.createPoint(new Coordinate(-1.762223, 51.174174)),
            geometryFactory.createPoint(new Coordinate(-1.479123, 51.209546)),
            geometryFactory.createPoint(new Coordinate(-1.77218, 51.198944)),
            geometryFactory.createPoint(new Coordinate(-1.449829, 51.278587)),
            geometryFactory.createPoint(new Coordinate(-1.499599, 51.204181)),
            geometryFactory.createPoint(new Coordinate(-1.73705, 51.188087)),
            geometryFactory.createPoint(new Coordinate(-1.621352, 51.169663)),
            geometryFactory.createPoint(new Coordinate(-1.483391, 51.205962)),
            geometryFactory.createPoint(new Coordinate(-1.479032, 51.209388)),
            geometryFactory.createPoint(new Coordinate(-1.481162, 51.208552)),
            geometryFactory.createPoint(new Coordinate(-1.808102, 51.158087)),
            geometryFactory.createPoint(new Coordinate(-1.66033, 51.234649)),
            geometryFactory.createPoint(new Coordinate(-1.660771, 51.234677)),
            geometryFactory.createPoint(new Coordinate(-1.441114, 51.319309)),
            geometryFactory.createPoint(new Coordinate(-1.562245, 51.207657)),
            geometryFactory.createPoint(new Coordinate(-1.568989, 51.212772)),
            geometryFactory.createPoint(new Coordinate(-1.763648, 51.164182)),
            geometryFactory.createPoint(new Coordinate(-1.54585, 51.216888)),
            geometryFactory.createPoint(new Coordinate(-1.50217, 51.21397)),
            geometryFactory.createPoint(new Coordinate(-1.661497, 51.234688)),
            geometryFactory.createPoint(new Coordinate(-1.535695, 51.213724)),
            geometryFactory.createPoint(new Coordinate(-1.575505, 51.205442)),
            geometryFactory.createPoint(new Coordinate(-1.777924, 51.171671)),
            geometryFactory.createPoint(new Coordinate(-1.479022, 51.206876)),
            geometryFactory.createPoint(new Coordinate(-1.508978, 51.211053)),
            geometryFactory.createPoint(new Coordinate(-1.765542, 51.168059)),
            geometryFactory.createPoint(new Coordinate(-1.770211, 51.195673)),
            geometryFactory.createPoint(new Coordinate(-1.391133, 51.254672)),
            geometryFactory.createPoint(new Coordinate(-1.543308, 51.217804)),
            geometryFactory.createPoint(new Coordinate(-1.482398, 51.203727)),
            geometryFactory.createPoint(new Coordinate(-1.780192, 51.172023)),
            geometryFactory.createPoint(new Coordinate(-1.735372, 51.194043)),
            geometryFactory.createPoint(new Coordinate(-1.660202, 51.234712)),
            geometryFactory.createPoint(new Coordinate(-1.660967, 51.234337)),
            geometryFactory.createPoint(new Coordinate(-1.482797, 51.205724)),
            geometryFactory.createPoint(new Coordinate(-1.577447, 51.209369)),
            geometryFactory.createPoint(new Coordinate(-1.579682, 51.217704)),
            geometryFactory.createPoint(new Coordinate(-1.782846, 51.195659)),
            geometryFactory.createPoint(new Coordinate(-1.767086, 51.168238)),
            geometryFactory.createPoint(new Coordinate(-1.76956, 51.194893)),
            geometryFactory.createPoint(new Coordinate(-1.501748, 51.206949)),
            geometryFactory.createPoint(new Coordinate(-1.765989, 51.175582)),
            geometryFactory.createPoint(new Coordinate(-1.669106, 51.190766)),
            geometryFactory.createPoint(new Coordinate(-1.479849, 51.206554)),
            geometryFactory.createPoint(new Coordinate(-1.735372, 51.194043)),
            geometryFactory.createPoint(new Coordinate(-1.491057, 51.21168)),
            geometryFactory.createPoint(new Coordinate(-1.49355, 51.204049)),
            geometryFactory.createPoint(new Coordinate(-1.478944, 51.209065)),
            geometryFactory.createPoint(new Coordinate(-1.478974, 51.206169)),
            geometryFactory.createPoint(new Coordinate(-1.668803, 51.207679)),
            geometryFactory.createPoint(new Coordinate(-1.780563, 51.178402)),
            geometryFactory.createPoint(new Coordinate(-1.481993, 51.212489)),
            geometryFactory.createPoint(new Coordinate(-1.568451, 51.214498)),
            geometryFactory.createPoint(new Coordinate(-1.7718, 51.198967)),
            geometryFactory.createPoint(new Coordinate(-1.474827, 51.214442)),
            geometryFactory.createPoint(new Coordinate(-1.606542, 51.205707)),
            geometryFactory.createPoint(new Coordinate(-1.757789, 51.176964)),
            geometryFactory.createPoint(new Coordinate(-1.474889, 51.214493)),
            geometryFactory.createPoint(new Coordinate(-1.782335, 51.172274)),
            geometryFactory.createPoint(new Coordinate(-1.653814, 51.240399)),
            geometryFactory.createPoint(new Coordinate(-1.545327, 51.217793)),
            geometryFactory.createPoint(new Coordinate(-1.765549, 51.163736)),
            geometryFactory.createPoint(new Coordinate(-1.481061, 51.205881)),
            geometryFactory.createPoint(new Coordinate(-1.780525, 51.172878)),
            geometryFactory.createPoint(new Coordinate(-1.762968, 51.176863)),
            geometryFactory.createPoint(new Coordinate(-1.780645, 51.173308)),
            geometryFactory.createPoint(new Coordinate(-1.758886, 51.177443)),
            geometryFactory.createPoint(new Coordinate(-1.502847, 51.208686)),
            geometryFactory.createPoint(new Coordinate(-1.445984, 51.281267)),
            geometryFactory.createPoint(new Coordinate(-1.480544, 51.206458)),
            geometryFactory.createPoint(new Coordinate(-1.548153, 51.218444)),
            geometryFactory.createPoint(new Coordinate(-1.422972, 51.264767)),
            geometryFactory.createPoint(new Coordinate(-1.478599, 51.206027)),
            geometryFactory.createPoint(new Coordinate(-1.756675, 51.17747)),
            geometryFactory.createPoint(new Coordinate(-1.773764, 51.202566)),
            geometryFactory.createPoint(new Coordinate(-1.547467, 51.218048)),
            geometryFactory.createPoint(new Coordinate(-1.513043, 51.211139)),
            geometryFactory.createPoint(new Coordinate(-1.476268, 51.205947)),
            geometryFactory.createPoint(new Coordinate(-1.505133, 51.220075)),
            geometryFactory.createPoint(new Coordinate(-1.661118, 51.234711)),
            geometryFactory.createPoint(new Coordinate(-1.514023, 51.211026)),
            geometryFactory.createPoint(new Coordinate(-1.770736, 51.176876)),
            geometryFactory.createPoint(new Coordinate(-1.760028, 51.190612)),
            geometryFactory.createPoint(new Coordinate(-1.482759, 51.210533)),
            geometryFactory.createPoint(new Coordinate(-1.661065, 51.234168)),
            geometryFactory.createPoint(new Coordinate(-1.730662, 51.190613)),
            geometryFactory.createPoint(new Coordinate(-1.478258, 51.207249)),
            geometryFactory.createPoint(new Coordinate(-1.608659, 51.208859)),
            geometryFactory.createPoint(new Coordinate(-1.497076, 51.207281)),
            geometryFactory.createPoint(new Coordinate(-1.453039, 51.276052)),
            geometryFactory.createPoint(new Coordinate(-1.481839, 51.225715)),
            geometryFactory.createPoint(new Coordinate(-1.503977, 51.208947)),
            geometryFactory.createPoint(new Coordinate(-1.478501, 51.206197)),
            geometryFactory.createPoint(new Coordinate(-1.779344, 51.173663)),
            geometryFactory.createPoint(new Coordinate(-1.480084, 51.208834)),
            geometryFactory.createPoint(new Coordinate(-1.497737, 51.219422)),
            geometryFactory.createPoint(new Coordinate(-1.486422, 51.21321)),
            geometryFactory.createPoint(new Coordinate(-1.604803, 51.176022)),
            geometryFactory.createPoint(new Coordinate(-1.759181, 51.176769)),
            geometryFactory.createPoint(new Coordinate(-1.782372, 51.171888)),
            geometryFactory.createPoint(new Coordinate(-1.661023, 51.234717)),
            geometryFactory.createPoint(new Coordinate(-1.763283, 51.192435)),
            geometryFactory.createPoint(new Coordinate(-1.662466, 51.23523)),
            geometryFactory.createPoint(new Coordinate(-1.473871, 51.21641)),
            geometryFactory.createPoint(new Coordinate(-1.479297, 51.222206)),
            geometryFactory.createPoint(new Coordinate(-1.761879, 51.173976)),
            geometryFactory.createPoint(new Coordinate(-1.660771, 51.234677)),
            geometryFactory.createPoint(new Coordinate(-1.564558, 51.237567)),
            geometryFactory.createPoint(new Coordinate(-1.763653, 51.163854)),
            geometryFactory.createPoint(new Coordinate(-1.662884, 51.234823)),
            geometryFactory.createPoint(new Coordinate(-1.788912, 51.195613)),
            geometryFactory.createPoint(new Coordinate(-1.7653, 51.163533)),
            geometryFactory.createPoint(new Coordinate(-1.494476, 51.213973)),
            geometryFactory.createPoint(new Coordinate(-1.497043, 51.207337)),
            geometryFactory.createPoint(new Coordinate(-1.668424, 51.184793)),
            geometryFactory.createPoint(new Coordinate(-1.78227, 51.172387)),
            geometryFactory.createPoint(new Coordinate(-1.779824, 51.173305)),
            geometryFactory.createPoint(new Coordinate(-1.481379, 51.207228)),
            geometryFactory.createPoint(new Coordinate(-1.397654, 51.25069)));

    return foodEstablishmentPoints;
  }

  public static List<Point> createPointsForChargersPoiTest() {

    List<Point> chargerPoints =
        List.of(
            geometryFactory.createPoint(new Coordinate(-1.42455459999996, 51.2901275)),
            geometryFactory.createPoint(new Coordinate(-1.4935647, 51.2138249)),
            geometryFactory.createPoint(new Coordinate(-1.519761, 51.213413)),
            geometryFactory.createPoint(new Coordinate(-1.607693, 51.205902)),
            geometryFactory.createPoint(new Coordinate(-1.441199, 51.319252)),
            geometryFactory.createPoint(new Coordinate(-1.521002, 51.213197)),
            geometryFactory.createPoint(new Coordinate(-1.391967, 51.246825)),
            geometryFactory.createPoint(new Coordinate(-1.535015, 51.213795)),
            geometryFactory.createPoint(new Coordinate(-1.5354433, 51.214881)),
            geometryFactory.createPoint(new Coordinate(-1.7555, 51.177601)),
            geometryFactory.createPoint(new Coordinate(-1.479853, 51.204827)),
            geometryFactory.createPoint(new Coordinate(-1.477911, 51.206867)),
            geometryFactory.createPoint(new Coordinate(-1.481754, 51.20814)),
            geometryFactory.createPoint(new Coordinate(-1.473453, 51.216769)),
            geometryFactory.createPoint(new Coordinate(-1.766382, 51.175978)),
            geometryFactory.createPoint(new Coordinate(-1.478989, 51.212002)),
            geometryFactory.createPoint(new Coordinate(-1.513828, 51.21098)),
            geometryFactory.createPoint(new Coordinate(-1.75714171113339, 51.1770568092747)),
            geometryFactory.createPoint(new Coordinate(-1.756329629088782, 51.177222921626964)),
            geometryFactory.createPoint(new Coordinate(-1.77905423119387, 51.1738926219467)),
            geometryFactory.createPoint(new Coordinate(-1.75839094040271, 51.1770625711239)),
            geometryFactory.createPoint(new Coordinate(-1.75903966021008, 51.1770575784368)),
            geometryFactory.createPoint(new Coordinate(-1.661898, 51.233933)),
            geometryFactory.createPoint(new Coordinate(-1.76038439417871, 51.1771454290478)),
            geometryFactory.createPoint(new Coordinate(-1.5188713, 51.20992)),
            geometryFactory.createPoint(new Coordinate(-1.626036297005129, 51.25096812063623)),
            geometryFactory.createPoint(new Coordinate(-1.7792164864164874, 51.172256412234844)),
            geometryFactory.createPoint(new Coordinate(-1.6678794663969492, 51.19135722554175)),
            geometryFactory.createPoint(new Coordinate(-1.4557245427471912, 51.211727661332446)));

    return chargerPoints;
  }

  public static void createFoodEstablishmentPricesRatingsPopularity(
      List<FoodEstablishment> foodEstablishments) {

    List<Integer> prices =
        Arrays.asList(
            1, null, 1, null, 1, 1, 1, 1, 1, 2, 2, null, 1, 1, null, 1, 3, null, 1, 1, 1, null, 1,
            2, null, null, null, 2, 1, null, 2, null, 1, 2, 1, 1, null, null, 2, 1, 3, 1, null,
            null, 2, 1, null, 1, null, 1, 1, 1, null, null, null, null, null, null, null, null, 1,
            1, 1, 3, 1, null, 1, 1, null, 1, 1, 2, 2, null, 1, 1, 3, 1, 2, null, 2, 1, null, null,
            1, 2, null, 2, null, 1, 2, null, 1, 1, null, 2, 2, null, 1, 1, null, 2, null, null, 3,
            1, null, null, null, null, 1, null, null, 2, 1, 1, 1, 3, null, 1, 1, 1, null, 3, null,
            1, 2, 1, 1, null, 1, 1, null, 1, 1, 1, 2, 1, 3, null, 1, null, 1, null, 1, 1);

    List<Double> popularity =
        Arrays.asList(
            0.42881446648396204,
            0.6463361344236966,
            0.9761232384290401,
            0.0732771580930423,
            0.7116308875360461,
            0.9285714285714286,
            0.9565217391304348,
            0.4152910841858882,
            0.02753156181266799,
            0.6923076923076923,
            0.30434782608695654,
            0.9986327583089737,
            0.93752002563281,
            0.991788443475657,
            0.9907081063761615,
            0.32880597652321275,
            0.7624633431085044,
            0.9884652355014418,
            0.9743671900032042,
            0.9727139310282749,
            0.053990575632124886,
            0.9930708878478742,
            0.949323986875339,
            0.31346730252722094,
            0.9027088344946083,
            0.9794937520025633,
            0.38796936760788453,
            0.21278237750099027,
            0.9644344761294457,
            0.7200881933031087,
            0.20269223815567367,
            0.987824415251522,
            0.9025128493765888,
            0.41603766371623485,
            0.6392181992950977,
            0.925344440884332,
            0.9942326177507209,
            0.23939909489858205,
            0.9648093841642229,
            0.1436950146627566,
            1.0,
            0.637504606187289,
            null,
            0.9658130761250285,
            0.6142857142857143,
            0.9637936558795258,
            0.5571847507331378,
            0.9854996840558281,
            null,
            0.3469387755102041,
            0.9923101570009613,
            0.8135213072733098,
            0.01020408163265306,
            0.17436742760070897,
            0.09179043810498962,
            0.4714285714285714,
            0.9987183595001602,
            0.7378594967754771,
            0.9772312038789914,
            0.7390029325513197,
            0.9560117302052786,
            0.9926305671259211,
            0.9611863799548958,
            0.5501205344276858,
            1.0,
            0.7755102040816326,
            0.5172398626160689,
            0.8289009932713873,
            0.9980962797350007,
            0.16467263401799412,
            0.9647548862544056,
            0.5252968609393016,
            0.9182954181352131,
            0.9698814482537648,
            0.19897468760012815,
            0.13072733098365907,
            0.9561006633165418,
            0.9804499474619748,
            0.18423582185197052,
            null,
            0.09904881753003004,
            0.9919897468760013,
            null,
            0.9863158198911086,
            0.8891380967638577,
            0.5461862303576959,
            null,
            1.0,
            0.32142857142857145,
            0.18808074335148992,
            0.9582859076113265,
            0.99499757192237,
            0.8308056947178891,
            0.9906018180279202,
            0.2742710669657161,
            0.9651949200371001,
            0.9381608458827299,
            null,
            0.6311381329399992,
            0.9429669977571291,
            0.9885947606201865,
            0.6644997405703259,
            0.21428571428571427,
            0.9830182633771227,
            0.49695610381288047,
            0.9793411381303828,
            null,
            0.02242870874719641,
            0.01795197085999978,
            0.9230769230769231,
            0.6702979814162128,
            0.9941348973607038,
            0.5747756105931183,
            0.9577058635052867,
            0.984263141730433,
            0.9186158282601731,
            1.0,
            0.9279077218840115,
            0.9955142582505607,
            0.9330342838833707,
            0.6094346223156573,
            0.9660365267542455,
            0.9957124163885906,
            0.7805190644024351,
            null,
            0.9978464651971704,
            0.9751351553164159,
            0.9970674486803519,
            0.8461538461538461,
            0.07624633431085044,
            0.9980775392502403,
            0.10092918936238385,
            0.6554416940438067,
            0.8357771260997068,
            0.009978425026968716,
            0.9856742396946069,
            0.8797653958944281,
            0.8519678169114392,
            1.0,
            0.9205382890099327,
            0.93719961550785,
            0.9701623653848956,
            0.9632712023344323,
            0.286029846303581,
            0.9679589875040051,
            1.0);

    List<Double> ratings =
        Arrays.asList(
            null, null, null, null, null, null, null, null, null, null, null, 5.2, null, 6.4, 6.3,
            null, null, 6.3, 6.0, 6.7, null, 6.2, null, null, null, 6.6, null, null, null, null,
            null, null, null, null, null, null, 7.2, null, null, null, 7.1, null, null, 6.2, null,
            null, null, 6.7, null, null, 5.7, null, 6.3, null, null, null, 5.8, null, null, null,
            6.3, null, 6.9, null, 8.0, null, null, null, 6.2, null, null, null, null, 6.3, null,
            null, null, 5.0, null, null, null, 6.3, null, 5.3, null, null, null, null, null, null,
            null, 6.8, null, 5.1, null, null, 7.9, null, null, null, 5.8, null, null, 6.4, null,
            null, null, null, null, null, null, 5.4, null, null, null, null, 8.1, null, 6.0, null,
            null, 6.3, 6.1, null, null, 5.6, null, null, null, 6.6, null, null, null, null, null,
            null, null, null, 6.2, 6.3, null, 7.2, null, null, 7.2, null);

    for (int i = 0; i < foodEstablishments.size(); i++) {
      foodEstablishments.get(i).setPrice(prices.get(i));
      foodEstablishments.get(i).setRating(ratings.get(i));
      foodEstablishments.get(i).setPopularity(popularity.get(i));
    }
  }

  public static List<FoodEstablishment> createFoodEstablishmentsForPoiTest() {
    List<FoodEstablishment> foodEstablishments = new ArrayList<>();
    List<Point> points = TestDataFactory.createPointsForFoodEstablishments();

    System.out.println("POINTS SIZE: " + points.size());

    for (int i = 0; i < points.size(); i++) {
      FoodEstablishment foodEstablishment = new FoodEstablishment();
      foodEstablishment.setId(String.valueOf(i));
      foodEstablishment.setLocation(points.get(i));
      foodEstablishment.setName("Food Establishment " + i);
      //
      //      if (i % 5 == 0 && i < 100) {
      //        foodEstablishment.setRating(1.0);
      //        foodEstablishment.setPopularity(1.0 + (i / points.size()));
      //      } else if (i == 11) {
      //        foodEstablishment.setRating(1000.0);
      //        foodEstablishment.setPopularity(1000.0);
      //      } else {
      //        foodEstablishment.setRating(0.5);
      //        foodEstablishment.setPopularity(0.5);
      //      }
      createFoodEstablishmentPricesRatingsPopularity(foodEstablishments);
      foodEstablishments.add(foodEstablishment);
    }

    return foodEstablishments;
  }
}
