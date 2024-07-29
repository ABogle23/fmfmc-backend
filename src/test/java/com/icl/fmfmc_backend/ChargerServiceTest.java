package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.repository.ChargerRepo;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import com.icl.fmfmc_backend.service.EvScraperService;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.List;

@SpringBootTest
@ActiveProfiles({"test"})
@EnabledIf(value = "#{environment.getActiveProfiles()[0] == 'test'}", loadContext = true)
public class ChargerServiceTest {

  @Value("${profile.property.value}")
  private String propertyString;

  @Autowired
  private ChargerService chargerService;

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  @Test
  public void testFindAllWithinPolygon() {


    List<Charger> chargers = chargerService.getAllChargers();
    System.out.println(chargers.size());


    Point andover =
        geometryFactory.createPoint(new Coordinate(-1.5146224977344835, 51.241816093755936));
    ChargerQuery query = ChargerQuery.builder().point(andover).radius(Double.valueOf(200000)).build();
    chargers = chargerService.getChargersByParams(query);
    System.out.println(chargers.size());

    andover =
            geometryFactory.createPoint(new Coordinate(-3.533333, 50.716667));
    query = ChargerQuery.builder().point(andover).radius(Double.valueOf(5000)).build();
    Charger adjacentCharger = chargerService.getNearestChargerByParams(query);
    System.out.println(adjacentCharger);

    chargers = chargerService.getChargersByParams(query);
    System.out.println(chargers.size());

  }
}
