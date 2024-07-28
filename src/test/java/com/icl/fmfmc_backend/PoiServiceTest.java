package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.exception.NoFoodEstablishmentsFoundException;
import com.icl.fmfmc_backend.exception.NoFoodEstablishmentsInRangeOfChargerException;
import com.icl.fmfmc_backend.exception.PoiServiceException;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import com.icl.fmfmc_backend.service.PoiService;
import com.icl.fmfmc_backend.util.TestDataFactory;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PoiServiceTest {

  @Mock private ChargerService chargerService;

  @Mock private FoodEstablishmentService foodEstablishmentService;

  @InjectMocks private PoiService poiService;

  private GeometryService geometryService;

  private final RouteRequest routeRequest = TestDataFactory.createDefaultRouteRequest();

  private final DirectionsResponse directionsResponse =
      TestDataFactory.createDefaultDirectionsResponse();

  private final Route route = new Route(directionsResponse, routeRequest);

  private final List<Point> chargersLocations = TestDataFactory.createPointsForChargersPoiTest();

  private final List<FoodEstablishment> foodEstablishmentLocations =
      TestDataFactory.createFoodEstablishmentsForPoiTest();

  @Test
  public void getFoodEstablishmentOnRouteReturnsFiveOptimalChoice() {

    route.setStopForEating(true);
    route.setIncludeAlternativeEatingOptions(true);

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
      result = poiService.getFoodEstablishmentOnRoute(route);
    } catch (NoFoodEstablishmentsFoundException e) {
      fail("NoFoodEstablishmentsFoundException should not have been thrown");
    } catch (NoFoodEstablishmentsInRangeOfChargerException e) {
      fail("NoFoodEstablishmentsInRangeofChargerException should not have been thrown");
    } catch (PoiServiceException e) {
      fail("PoiServiceException should not have been thrown");
    }

    assertEquals(5, result.getT1().size());

    assertEquals("144", result.getT1().get(0).getId());
    assertEquals("96", result.getT1().get(1).getId());
    assertEquals("121", result.getT1().get(2).getId());
    assertEquals("36", result.getT1().get(3).getId());
    assertEquals("50", result.getT1().get(4).getId());

    for (FoodEstablishment foodEstablishment : result.getT1()) {
      Double chargerFoodEstablishmentDistance =
          GeometryService.calculateDistanceBetweenPoints(
              foodEstablishment.getLocation(), result.getT2().getLocation());

      assertTrue(chargerFoodEstablishmentDistance < route.getMaxWalkingDistance());
      System.out.println("ChargerFoodEstablishmentDistance: " + chargerFoodEstablishmentDistance);
    }

    for (FoodEstablishment foodEstablishment : result.getT1()) {
      System.out.println("FoodEstablishment: " + foodEstablishment.getName());
    }
  }
}
