package com.icl.fmfmc_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.icl.fmfmc_backend.geometry_service.GeometryService;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.entity.enums.DeviationScope;
import com.icl.fmfmc_backend.exception.integration.ServiceUnavailableException;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsFoundException;
import com.icl.fmfmc_backend.exception.service.NoFoodEstablishmentsInRangeOfChargerException;
import com.icl.fmfmc_backend.exception.service.PoiServiceException;
import com.icl.fmfmc_backend.util.TestDataFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.util.function.Tuple2;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PoiServiceTest {

  @Mock private ChargerService chargerService;
  @Mock private FoodEstablishmentService foodEstablishmentService;
  @InjectMocks private PoiService poiService;

  private final Journey journey =
      new Journey(
          TestDataFactory.createDefaultDirectionsResponse(),
          TestDataFactory.createDefaultJourneyRequest());

  private final List<Point> chargersLocations = TestDataFactory.createPointsForChargersPoiTest();

  private final List<FoodEstablishment> foodEstablishmentLocations =
      TestDataFactory.createFoodEstablishmentsForPoiTest();

  @BeforeEach
  public void setRequestRelatedParams() {
    journey.setStopForEating(true);
    journey.setChargerSearchDeviation(DeviationScope.moderate);
    journey.setEatingOptionSearchDeviation(DeviationScope.moderate);
  }

  @Test
  public void getFoodEstablishmentOnRouteReturnsFiveOptimalChoices() {

    journey.setIncludeAlternativeEatingOptions(true);

    Mockito.when(chargerService.getChargerLocationsByParams(any()))
        .thenReturn(TestDataFactory.createPointsForChargersPoiTest());
    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(TestDataFactory.createFoodEstablishmentsForPoiTest());
    Charger adjacentCharger = new Charger();
    adjacentCharger.setLocation(
        new GeometryFactory().createPoint(new Coordinate(-1.481754, 51.20814)));
    adjacentCharger.setId(1L);
    Mockito.when(chargerService.getNearestChargerByParams(any())).thenReturn(adjacentCharger);

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
    assertEquals("17", result.getT1().get(4).getId());

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
  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsNoFoodEstablishmentsInRangeOfChargerException() {

    FoodEstablishment outOfRangeFe1 =
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.490539557859501, 51.197202);

    Mockito.when(chargerService.getChargerLocationsByParams(any()))
        .thenReturn(TestDataFactory.createPointsForChargersPoiTest());
    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(List.of(outOfRangeFe1));

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
        TestDataFactory.createDefaultFoodEstablishment("1", "Food1", -1.49053, 51.19722);

    Charger chargerInFallback = TestDataFactory.createDefaultCharger(1000L, 1, -1.49054, 51.19723);
    Charger chargerInFallback1 = TestDataFactory.createDefaultCharger(2000L, 1, -1.49100, 51.19600);

    List<Point> chargerServiceCallInFallback =
        new ArrayList<>(TestDataFactory.createPointsForChargersPoiTest());
    chargerServiceCallInFallback.add(chargerInFallback.getLocation());
    chargerServiceCallInFallback.add(chargerInFallback1.getLocation());

    Mockito.when(chargerService.getChargerLocationsByParams(any()))
        .thenReturn(TestDataFactory.createPointsForChargersPoiTest())
        .thenReturn(chargerServiceCallInFallback);
    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(List.of(outOfRangeFe1));

    Mockito.when(chargerService.getNearestChargerByParams(any())).thenReturn(chargerInFallback);

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
    assertEquals(chargerInFallback.getId(), result.getT2().getId());
  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsNoFoodEstablishmentsFoundException() {

    Mockito.when(chargerService.getChargerLocationsByParams(any()))
            .thenReturn(TestDataFactory.createPointsForChargersPoiTest());

    Mockito.when(foodEstablishmentService.getFoodEstablishmentsByParam(any()))
        .thenReturn(Collections.emptyList());

    Tuple2<List<FoodEstablishment>, Charger> result = null;

    NoFoodEstablishmentsFoundException thrown =
        assertThrows(
            NoFoodEstablishmentsFoundException.class,
            () -> poiService.getFoodEstablishmentOnRoute(journey),
            "Expected NoFoodEstablishmentsFoundException to throw, it didn't");
  }

  @Test
  public void getFoodEstablishmentOnRouteThrowsPoiServiceException() {

    Mockito.when(chargerService.getChargerLocationsByParams(any()))
            .thenReturn(TestDataFactory.createPointsForChargersPoiTest());

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
