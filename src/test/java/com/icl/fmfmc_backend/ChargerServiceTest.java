package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.config.TestContainerConfig;
import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Charger.Connection;
import com.icl.fmfmc_backend.service.ChargerService;
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

    Charger charger1 = new Charger();
    charger1.setId(1L);
    charger1.setNumberOfPoints(3L);
    charger1.setStatusTypeID(50L);
    charger1.setUsageTypeID(25L);
    charger1.setLocation(geometryFactory.createPoint(new Coordinate(0, 0)));

    List<Connection> connections1 = new ArrayList<>();
    Connection connection1 = new Connection();
    connection1.setId(1L);
    connection1.setConnectionTypeID(25L);
    connection1.setPowerKW(50L);
    connections1.add(connection1);

    Connection connection2 = new Connection();
    connection2.setId(1L);
    connection2.setConnectionTypeID(25L);
    connection2.setPowerKW(50L);
    connections1.add(connection2);

    Connection connection3 = new Connection();
    connection3.setId(1L);
    connection3.setConnectionTypeID(25L);
    connection3.setPowerKW(50L);
    connections1.add(connection3);

    charger1.setConnections(connections1);

    chargerService.saveCharger(charger1);

    Charger savedCharger = chargerService.getChargerById(charger1.getId());
    assertNotNull(savedCharger);
    assertEquals(charger1.getId(), savedCharger.getId());
  }

  @Test
  @Transactional
  public void canUpdateCharger() {
    addTestData(LITE_DATA);
    List<Charger> chargers = chargerService.getAllChargers();
    assertNotNull(chargers);
    assertFalse(chargers.isEmpty());
  }

  @Test
  @Transactional
  public void canUpdateChargersInBatches() {
    addTestData(LITE_DATA);
    List<Charger> chargers = chargerService.getAllChargers();
    assertNotNull(chargers);
    assertFalse(chargers.isEmpty());
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
  public void getAllChargersWithinPolygon() {
    addTestData(LITE_DATA);
    Polygon polygon = getPolygon();
    List<Charger> chargers = chargerService.getChargersWithinPolygon(polygon);
    assertNotNull(chargers);
    assertFalse(chargers.isEmpty());
  }

  @Test
  @Transactional
  public void testFindAllWithinPolygon() {

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
