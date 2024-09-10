package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.config.TestContainerConfig;
import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.charger.Connection;
import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.entity.enums.AccessType;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// @ActiveProfiles({"test"})
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestContainerConfig.class)
public class ChargerServiceTest {

  String HEAVY_DATA = "build_Test_DB_Data.sql";
  String LITE_DATA = "build_Test_DB_Data_lite.sql";

  @Autowired private ChargerService chargerService;

  @Autowired private TestContainerConfig testContainerConfig;

  @Autowired private DataSource dataSource;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  private void addTestData(String sqlFile) {
    testContainerConfig.loadData(dataSource, sqlFile);
  }

  private Polygon getPolygon() {
    Coordinate[] coordinates =
        new Coordinate[] {
          new Coordinate(0, 0),
          new Coordinate(0, 5),
          new Coordinate(5, 5),
          new Coordinate(5, 0),
          new Coordinate(0, 0)
        };
    LinearRing ring = geometryFactory.createLinearRing(coordinates);
    return geometryFactory.createPolygon(ring);
  }

  //  @AfterEach
  //  public void cleanUp() {
  ////    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
  ////    jdbcTemplate.update("TRUNCATE TABLE chargers;");
  ////    jdbcTemplate.update("TRUNCATE TABLE charger_connections;");
  ////    jdbcTemplate.update("TRUNCATE TABLE address_info;");
  ////    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
  //  testContainerConfig.clearTables(jdbcTemplate);
  //  }

  @Test
  @Transactional
  public void canSaveCharger() {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);

    chargerService.saveCharger(charger1);

    Charger savedCharger = chargerService.getChargerById(charger1.getId());
    assertNotNull(savedCharger);
    assertEquals(charger1.getId(), savedCharger.getId());
  }

  @Test
  @Transactional
  public void getChargerByIdReturnsNullIfNotInDb() {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);

    Charger notSavedCharger = chargerService.getChargerById(charger1.getId());
    assertNull(notSavedCharger);
  }

  @Test
  @Transactional
  public void canUpdateCharger() {
    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);
    charger1.setNumberOfPoints(3L);
    chargerService.saveCharger(charger1);
    charger1.setNumberOfPoints(6L);
    chargerService.updateCharger(charger1);

    Charger updatedCharger = chargerService.getChargerById(charger1.getId());
    assertNotNull(updatedCharger);
    assertEquals(charger1.getId(), updatedCharger.getId());
    assertEquals(charger1.getNumberOfPoints(), updatedCharger.getNumberOfPoints());
  }

  @Test
  @Transactional
  public void cannotUpdateChargerNotInDb() {
    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);
    charger1.setNumberOfPoints(6L);
    Charger updatedCharger = chargerService.updateCharger(charger1);

    assertNull(updatedCharger);
  }

  @Test
  @Transactional
  public void canDeleteCharger() {
    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);

    chargerService.saveCharger(charger1);
    chargerService.deleteChargerById(charger1.getId());
    Charger deletedCharger = chargerService.getChargerById(charger1.getId());
    assertNull(deletedCharger);
  }

  @Test
  @Transactional
  public void canCreateChargersInBatches() {
    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0, 0.0));
    }
    chargerService.saveChargersInBatch(chargers);

    List<Charger> savedChargers = chargerService.getAllChargers();
    assertNotNull(savedChargers);
    assertEquals(10, savedChargers.size());
  }

  @Test
  @Transactional
  public void canUpdateChargersInBatches() {
    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0, 0.0));
    }
    chargerService.saveChargersInBatch(chargers);
    chargerService.saveChargersInBatch(chargers); // repeat to trigger update

    List<Charger> savedChargers = chargerService.getAllChargers();
    assertNotNull(savedChargers);
    assertEquals(10, savedChargers.size());
  }

  @Test
  @Transactional
  public void canUpdateNullConnectionPowerKW() {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);

    for (Connection connection : charger1.getConnections()) {
      connection.setPowerKW(null);
    }

    chargerService.saveCharger(charger1);

    chargerService.updateNullConnectionPowerKW();

    Charger updatedCharger = chargerService.getChargerById(charger1.getId());
    assertNotNull(updatedCharger);
    assertEquals(charger1.getId(), updatedCharger.getId());

    for (Connection connection : updatedCharger.getConnections()) {
      assertNotNull(connection);
    }
  }

  @Test
  public void canGetHighestPowerConnectionByTypeInCharger() {

    Charger charger1 = TestDataFactory.createDefaultCharger(1L, 3, 0.0, 0.0);

    Journey journey = TestDataFactory.createDefaultRoute();
    journey.setConnectionTypes(List.of(ConnectionType.CCS)); // 33L

    for (Connection connection : charger1.getConnections()) {
      connection.setConnectionTypeID(33L);
    }

    charger1.getConnections().get(0).setConnectionTypeID(33L); // highest
    charger1.getConnections().get(0).setPowerKW(200L);
    charger1.getConnections().get(1).setConnectionTypeID(33L);
    charger1.getConnections().get(1).setPowerKW(100L);
    charger1.getConnections().get(2).setConnectionTypeID(3L);
    charger1.getConnections().get(2).setPowerKW(350L);

    chargerService.saveCharger(charger1);

    Double highestPowerCompatibleKw =
        chargerService.getHighestPowerConnectionByTypeInCharger(charger1, journey);

    assertNotNull(highestPowerCompatibleKw);
    assertEquals(200.0, highestPowerCompatibleKw, 0.001);
  }

  @Test
  @Transactional
  public void getAllChargersReturnsAllChargers() {
    addTestData(LITE_DATA);
    List<Charger> chargers = chargerService.getAllChargers();
    assertNotNull(chargers);
    assertFalse(chargers.isEmpty());
  }

  @Test
  @Transactional
  public void canGetAllChargersWithinPolygon() {
    Polygon polygon = getPolygon();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }
    chargerService.saveChargersInBatch(chargers);

    Charger chargerInPolygon1 = TestDataFactory.createDefaultCharger(5L, 2, 2.0, 2.0);
    Charger chargerInPolygon2 = TestDataFactory.createDefaultCharger(6L, 2, 3.0, 3.0);
    chargerService.saveCharger(chargerInPolygon1);
    chargerService.saveCharger(chargerInPolygon2);

    List<Charger> chargersInPolygon = chargerService.getChargersWithinPolygon(polygon);

    assertEquals(2, chargersInPolygon.size());
  }

  @Test
  @Transactional
  public void canGetChargersByConnectionType() {
    Polygon polygon = getPolygon();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0, 0.0));
    }

    chargers.get(3).getConnections().get(0).setConnectionTypeID(1036L); // not in query
    chargers.get(4).getConnections().get(0).setConnectionTypeID(33L);
    chargers.get(5).getConnections().get(0).setConnectionTypeID(27L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByConnectionType =
        chargerService.getChargersByConnectionType(
            List.of(ConnectionType.CCS, ConnectionType.TESLA));

    assertEquals(2, chargersByConnectionType.size());
  }

  @Test
  @Transactional
  public void canGetChargersByChargeSpeed() {

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0, 0.0));
    }

    chargers.get(3).getConnections().get(0).setPowerKW(350L); // not in query
    chargers.get(4).getConnections().get(0).setPowerKW(100L);
    chargers.get(5).getConnections().get(0).setPowerKW(90L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByPowerKw = chargerService.getChargersByChargeSpeed(60, 200);

    assertEquals(2, chargersByPowerKw.size());
  }

  @Test
  @Transactional
  public void canGetChargersByMinNoChargePoints() {

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0, 0.0));
    }

    chargers.get(3).setNumberOfPoints(1L); // not in query
    chargers.get(4).setNumberOfPoints(5L);
    chargers.get(5).setNumberOfPoints(10L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByNoChargePoints = chargerService.getChargersByMinNoChargePoints(5);

    assertEquals(2, chargersByNoChargePoints.size());
  }

  @Test
  @Transactional
  public void canGetChargersWithinRadius() {
    Point point = geometryFactory.createPoint(new Coordinate(0, 0));
    Double radius = 1000.0;

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }

    chargers.get(3).setLocation(geometryFactory.createPoint(new Coordinate(0.00005, 0.00005)));
    chargers.get(4).setLocation(geometryFactory.createPoint(new Coordinate(-0.00005, -0.00005)));
    chargers
        .get(5)
        .setLocation(
            geometryFactory.createPoint(new Coordinate(0.01, 0.01))); // approx 1.11km not in query

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersInPolygon = chargerService.getChargersWithinRadius(point, radius);

    assertEquals(2, chargersInPolygon.size());
  }

  @Test
  @Transactional
  public void canGetAllChargersWithinBoundingBox() {

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      // all outside BB except first
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 9.5 + i, 9.5 + i));
    }
    chargerService.saveChargersInBatch(chargers);

    // Chargers inside the bounding box
    Charger chargerInBoundingBox1 =
        TestDataFactory.createDefaultCharger(5L, 2, 5.0, 5.0); // Within bounding box
    Charger chargerInBoundingBox2 =
        TestDataFactory.createDefaultCharger(6L, 2, 5.0, 5.0); // Within bounding box
    chargerService.saveCharger(chargerInBoundingBox1);
    chargerService.saveCharger(chargerInBoundingBox2);

    // Create ChargerQuery using bounding box coordinates
    ChargerQuery query =
        ChargerQuery.builder().topLeftLatLng(0.0, 10.0).bottomRightLatLng(10.0, 0.0).build();

    // Retrieve chargers within bounding box
    List<Charger> chargersInBoundingBox = chargerService.getChargersByParams(query);

    // Assert results
    assertEquals(
        3,
        chargersInBoundingBox.size(),
        "Expected 3 chargers to be retrieved from the bounding box");
  }

  /*************************************
   * TESTs for multiple param queries
   **************************************/

  @Test
  @Transactional
  public void canGetAllChargersWithinBoundingBoxWithMultipleParams() {

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      // all outside BB
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 20.0 + i, 20.0 + i));
    }
    chargerService.saveChargersInBatch(chargers);

    // Chargers inside the bounding box
    Charger chargerInQuery1 =
        TestDataFactory.createDefaultCharger(5L, 2, 5.0, 5.0); // Within bounding box
    chargerInQuery1.getConnections().get(0).setPowerKW(100L);
    chargerInQuery1.setNumberOfPoints(5L);

    Charger chargerInQuery2 =
        TestDataFactory.createDefaultCharger(6L, 2, 5.0, 5.0); // Within bounding box
    chargerInQuery2.getConnections().get(0).setPowerKW(100L);
    chargerInQuery2.setNumberOfPoints(5L);

    Charger chargerInBoundingBoxButNotQuery =
        TestDataFactory.createDefaultCharger(7L, 2, 5.0, 5.0); // Within bounding box
    chargerInBoundingBoxButNotQuery.getConnections().get(0).setPowerKW(5L);

    Charger chargerInQueryButNotBoundingBox =
        TestDataFactory.createDefaultCharger(8L, 2, 100.0, 100.0); // Within bounding box
    chargerInQueryButNotBoundingBox.getConnections().get(0).setPowerKW(100L);
    chargerInQueryButNotBoundingBox.setNumberOfPoints(5L);

    chargerService.saveCharger(chargerInQuery1);
    chargerService.saveCharger(chargerInQuery2);
    chargerService.saveCharger(chargerInBoundingBoxButNotQuery);
    chargerService.saveCharger(chargerInQueryButNotBoundingBox);

    // Create ChargerQuery using bounding box coordinates
    ChargerQuery query =
        ChargerQuery.builder()
            .topLeftLatLng(0.0, 10.0)
            .bottomRightLatLng(10.0, 0.0)
            .minKwChargeSpeed(75)
            .minNoChargePoints(4)
            .build();

    // Retrieve chargers within bounding box
    List<Charger> chargersInBoundingBox = chargerService.getChargersByParams(query);

    // Assert results
    assertEquals(
        2,
        chargersInBoundingBox.size(),
        "Expected 2 chargers to be retrieved from the bounding box");
  }

  @Test
  @Transactional
  public void canGetAllChargersByParamWithinPolygon() {
    Polygon polygon = getPolygon();

    ChargerQuery query = ChargerQuery.builder().polygon(polygon).build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }
    chargerService.saveChargersInBatch(chargers);

    Charger chargerInPolygon1 = TestDataFactory.createDefaultCharger(5L, 2, 2.0, 2.0);
    Charger chargerInPolygon2 = TestDataFactory.createDefaultCharger(6L, 2, 3.0, 3.0);
    chargerService.saveCharger(chargerInPolygon1);
    chargerService.saveCharger(chargerInPolygon2);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(2, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(2, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetChargersByParamConnectionType() {

    ChargerQuery query =
        ChargerQuery.builder()
            .connectionTypeIds(List.of(ConnectionType.CCS, ConnectionType.TESLA))
            .build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0 + i, 0.0 + i));
    }

    chargers.get(3).getConnections().get(0).setConnectionTypeID(1036L); // not in query
    chargers.get(4).getConnections().get(0).setConnectionTypeID(33L);
    chargers.get(5).getConnections().get(0).setConnectionTypeID(27L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(2, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(2, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetChargersByParamChargeSpeed() {

    ChargerQuery query = ChargerQuery.builder().minKwChargeSpeed(60).maxKwChargeSpeed(200).build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0 + i, 0.0 + i));
    }

    chargers.get(3).getConnections().get(0).setPowerKW(350L); // not in query
    chargers.get(4).getConnections().get(0).setPowerKW(100L);
    chargers.get(5).getConnections().get(0).setPowerKW(90L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(2, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(2, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetChargersByParamMinNoChargePoints() {

    ChargerQuery query = ChargerQuery.builder().minNoChargePoints(5).build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 0.0 + i, 0.0 + i));
    }

    chargers.get(3).setNumberOfPoints(1L); // not in query
    chargers.get(4).setNumberOfPoints(5L);
    chargers.get(5).setNumberOfPoints(10L);

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(2, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(2, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetChargersByParamWithinRadius() {
    Point point = geometryFactory.createPoint(new Coordinate(0, 0));
    Double radius = 1000.0;

    ChargerQuery query = ChargerQuery.builder().point(point).radius(radius).build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }

    chargers.get(3).setLocation(geometryFactory.createPoint(new Coordinate(0.00005, 0.00005)));
    chargers.get(4).setLocation(geometryFactory.createPoint(new Coordinate(-0.00005, -0.00005)));
    chargers
        .get(5)
        .setLocation(
            geometryFactory.createPoint(new Coordinate(0.01, 0.01))); // approx 1.11km not in query

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(2, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(2, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetChargersWithMultipleParams() {
    Point point = geometryFactory.createPoint(new Coordinate(0, 0));
    Double radius = 1000.0;
    List<ConnectionType> connectionTypeIds = List.of(ConnectionType.CCS, ConnectionType.TESLA);
    List<AccessType> accessTypeIds = List.of(AccessType.PUBLIC);
    Integer minKwChargeSpeed = 60;
    Integer maxKwChargeSpeed = 200;
    Integer minNoChargePoints = 5;

    ChargerQuery query =
        ChargerQuery.builder()
            .point(point)
            .radius(radius)
            .connectionTypeIds(connectionTypeIds)
            .accessTypeIds(accessTypeIds)
            .minKwChargeSpeed(minKwChargeSpeed)
            .maxKwChargeSpeed(maxKwChargeSpeed)
            .minNoChargePoints(minNoChargePoints)
            .build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }

    chargers.get(3).setLocation(geometryFactory.createPoint(new Coordinate(0.00005, 0.00005)));
    chargers.get(3).setNumberOfPoints(7L);
    chargers.get(3).getConnections().get(0).setPowerKW(100L);
    chargers.get(3).getConnections().get(0).setConnectionTypeID(33L);
    chargers.get(3).setUsageTypeID(1L);
    chargers.get(4).setLocation(geometryFactory.createPoint(new Coordinate(-0.00005, -0.00005)));
    chargers.get(4).setNumberOfPoints(7L);
    chargers.get(4).getConnections().get(0).setPowerKW(100L);
    chargers.get(4).getConnections().get(0).setConnectionTypeID(2L); // not in query
    chargers.get(4).setUsageTypeID(1L);
    chargers
        .get(5)
        .setLocation(
            geometryFactory.createPoint(new Coordinate(0.01, 0.01))); // approx 1.11km not in query

    chargerService.saveChargersInBatch(chargers);

    List<Charger> chargersByParams = chargerService.getChargersByParams(query);
    assertEquals(1, chargersByParams.size());

    List<Point> chargerLocations = chargerService.getChargerLocationsByParams(query);
    assertEquals(1, chargerLocations.size());
  }

  @Test
  @Transactional
  public void canGetNearestChargerByParams() {
    Point point = geometryFactory.createPoint(new Coordinate(0, 0));
    Double radius = 1000.0;
    List<ConnectionType> connectionTypeIds = List.of(ConnectionType.CCS, ConnectionType.TESLA);
    List<AccessType> accessTypeIds = List.of(AccessType.PUBLIC);
    Integer minKwChargeSpeed = 60;
    Integer maxKwChargeSpeed = 200;
    Integer minNoChargePoints = 5;

    ChargerQuery query =
        ChargerQuery.builder()
            .point(point)
            .radius(radius)
            .connectionTypeIds(connectionTypeIds)
            .accessTypeIds(accessTypeIds)
            .minKwChargeSpeed(minKwChargeSpeed)
            .maxKwChargeSpeed(maxKwChargeSpeed)
            .minNoChargePoints(minNoChargePoints)
            .build();

    List<Charger> chargers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      chargers.add(TestDataFactory.createDefaultCharger((long) i, 2, 6.0 + i, 6.0 + i));
    }

    chargers.get(3).setLocation(geometryFactory.createPoint(new Coordinate(0.00005, 0.00005)));
    chargers.get(3).setNumberOfPoints(7L);
    chargers.get(3).getConnections().get(0).setPowerKW(100L);
    chargers.get(3).getConnections().get(0).setConnectionTypeID(33L);
    chargers.get(3).setUsageTypeID(1L);
    chargers
        .get(4)
        .setLocation(geometryFactory.createPoint(new Coordinate(-0.00001, -0.00001))); // closer
    chargers.get(4).setNumberOfPoints(7L);
    chargers.get(4).getConnections().get(0).setPowerKW(100L);
    chargers.get(4).getConnections().get(0).setConnectionTypeID(2L); // not in query
    chargers.get(4).setUsageTypeID(1L);
    chargers.get(5).setLocation(geometryFactory.createPoint(new Coordinate(0.01, 0.01)));

    chargerService.saveChargersInBatch(chargers);

    Charger nearestCharger = chargerService.getNearestChargerByParams(query);
    assertEquals(chargers.get(3).getId(), nearestCharger.getId());
  }

  @Test
  @Transactional
  public void testFindAllWithinPolygonWithRichData() {

    addTestData(HEAVY_DATA);

    List<Charger> chargers = chargerService.getAllChargers();
    System.out.println(chargers.size());

    Point andover =
        geometryFactory.createPoint(new Coordinate(-1.5146224977344835, 51.241816093755936));
    ChargerQuery query =
        ChargerQuery.builder().point(andover).radius(Double.valueOf(200000)).build();
    chargers = chargerService.getChargersByParams(query);
    System.out.println(chargers.size());

    andover = geometryFactory.createPoint(new Coordinate(-3.533333, 50.716667));
    query = ChargerQuery.builder().point(andover).radius(Double.valueOf(5000)).build();
    Charger adjacentCharger = chargerService.getNearestChargerByParams(query);
    System.out.println(adjacentCharger);

    chargers = chargerService.getChargersByParams(query);
    System.out.println(chargers.size());
  }
}
