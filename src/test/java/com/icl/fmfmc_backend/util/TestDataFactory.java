package com.icl.fmfmc_backend.util;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Charger.Connection;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
import com.icl.fmfmc_backend.entity.enums.StoppingRange;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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

    return directionsResponse;
  }

  //    public static RouteRequest createDefaultRoute() {
  //        Route route = new Route();
  //
  //        // add more properties as needed
  //        return route;
  //    }

  public static List<Charger> createChargers() {

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

  public static List<Charger> createChargersForBatteryExceptionTest() {

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
}
