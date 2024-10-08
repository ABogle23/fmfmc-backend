package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.geometry_service.GeometryService;
import com.icl.fmfmc_backend.config.TestContainerConfig;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
import com.icl.fmfmc_backend.exception.integration.ServiceUnavailableException;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsFoundException;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsInRangeOfChargerException;
import com.icl.fmfmc_backend.exception.service.PoiServiceException;
import com.icl.fmfmc_backend.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import reactor.util.function.Tuple2;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)
@SpringBootTest
// @ContextConfiguration(classes = {TestContainerConfig.class, TestDataLoaderConfig.class})
@ContextConfiguration(classes = {TestContainerConfig.class})
public class PoiServiceIntegrationTest {

  String HEAVY_DATA = "build_Test_DB_Data.sql";
  String LITE_DATA = "build_Test_DB_Data_lite.sql";

  //  @Mock private ChargerService chargerService;

  @Autowired private ChargerService chargerService;

  @Autowired private TestContainerConfig testContainerConfig;

  @Autowired private DataSource dataSource;

  @MockBean private FoodEstablishmentService foodEstablishmentService;

  @Autowired private PoiService poiService;

  @Autowired private JdbcTemplate jdbcTemplate;

  private void addTestData(String sqlFile) {
    testContainerConfig.loadData(dataSource, sqlFile);
  }

  private final Journey journey =
      new Journey(
          TestDataFactory.createDefaultDirectionsResponse(),
          TestDataFactory.createDefaultJourneyRequest());

  @BeforeEach
  public void setRequestRelatedParams() {
    journey.setStopForEating(true);
    journey.setChargerSearchDeviation(DeviationScope.moderate);
    journey.setEatingOptionSearchDeviation(DeviationScope.moderate);
  }

  @BeforeEach
  public void setUp() {
    addTestData(HEAVY_DATA);
  }

  @AfterEach
  public void tearDown() {
    testContainerConfig.clearTables(jdbcTemplate);
  }

  @Test
  public void getFoodEstablishmentOnRouteReturnsFiveOptimalChoices() {

    journey.setIncludeAlternativeEatingOptions(true);

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(TestDataFactory.createFoodEstablishmentsForPoiTest());
    //    Charger adjacentCharger = new Charger();
    //    adjacentCharger.setLocation(
    //        new GeometryFactory().createPoint(new Coordinate(-1.481754, 51.20814)));
    //    adjacentCharger.setId(1L);

    Tuple2<List<FoodEstablishment>, Charger> result = null;
    try {
      result = poiService.getFoodEstablishmentOnRoute(journey);
    } catch (NoFoodEstablishmentsFoundException e) {
      fail("NoFoodEstablishmentsFoundException should not have been thrown");
    } catch (NoFoodEstablishmentsInRangeOfChargerException e) {
      fail("NoFoodEstablishmentsInRangeofChargerException should not have been thrown");
    } catch (PoiServiceException e) {
      fail("PoiServiceException should not have been thrown");
    }

    assertEquals(5, result.getT1().size());
    assertEquals("96", result.getT1().get(0).getId());
    assertEquals("144", result.getT1().get(1).getId());
    assertEquals("36", result.getT1().get(2).getId());
    assertEquals("121", result.getT1().get(3).getId());
    assertEquals("50", result.getT1().get(4).getId());

    for (FoodEstablishment foodEstablishment : result.getT1()) {
      Double chargerFoodEstablishmentDistance =
          GeometryService.calculateDistanceBetweenPoints(
              foodEstablishment.getLocation(), result.getT2().getLocation());

      assertTrue(chargerFoodEstablishmentDistance < journey.getMaxWalkingDistance());
      System.out.println("ChargerFoodEstablishmentDistance: " + chargerFoodEstablishmentDistance);
    }

    for (FoodEstablishment foodEstablishment : result.getT1()) {
      System.out.println("FoodEstablishment: " + foodEstablishment.getName());
    }

    System.out.println("Charger: " + result.getT2().getLocation());

  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsNoFoodEstablishmentsInRangeOfChargerException() {

    FoodEstablishment outOfRangeFe1 =
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.490539557859501, 51.197202);

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(List.of(outOfRangeFe1));

    Charger adjacentCharger = new Charger();
    adjacentCharger.setLocation(
        new GeometryFactory().createPoint(new Coordinate(-1.481754, 51.20814)));
    adjacentCharger.setId(1L);

    Tuple2<List<FoodEstablishment>, Charger> result = null;

    NoFoodEstablishmentsInRangeOfChargerException thrown =
        assertThrows(
            NoFoodEstablishmentsInRangeOfChargerException.class,
            () -> poiService.getFoodEstablishmentOnRoute(journey),
            "Expected findSuitableChargers to throw, it didn't");
  }

  @Test
  public void getFoodEstablishmentOnRouteTriggersSuccessfulFallback() {

    FoodEstablishment outOfRangeFe1 =
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.488499, 51.114205);

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(List.of(outOfRangeFe1));

    Tuple2<List<FoodEstablishment>, Charger> result = null;
    try {
      result = poiService.getFoodEstablishmentOnRoute(journey);
    } catch (NoFoodEstablishmentsFoundException e) {
      fail("NoFoodEstablishmentsFoundException should not have been thrown");
    } catch (NoFoodEstablishmentsInRangeOfChargerException e) {
      fail("NoFoodEstablishmentsInRangeofChargerException should not have been thrown");
    } catch (PoiServiceException e) {
      fail("PoiServiceException should not have been thrown");
    }

    assertEquals(1, result.getT1().size());
    assertEquals("1", result.getT1().get(0).getId());
    assertNotNull(result.getT2().getId());
  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsNoFoodEstablishmentsFoundException() {

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(Collections.emptyList());

    Tuple2<List<FoodEstablishment>, Charger> result = null;

    NoFoodEstablishmentsFoundException thrown =
        assertThrows(
            NoFoodEstablishmentsFoundException.class,
            () -> poiService.getFoodEstablishmentOnRoute(journey),
            "Expected findSuitableChargers to throw, it didn't");
  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsPoiServiceException() {

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenThrow(new ServiceUnavailableException("Service Unavailable"));

    Tuple2<List<FoodEstablishment>, Charger> result = null;

    PoiServiceException thrown =
        assertThrows(
            PoiServiceException.class,
            () -> poiService.getFoodEstablishmentOnRoute(journey),
            "Expected findSuitableChargers to throw, it didn't");
  }
}
