package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.config.TestContainerConfig;
import com.icl.fmfmc_backend.dto.Api.ElectricVehicleDto;
import com.icl.fmfmc_backend.dto.ElectricVehicle;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.service.ElectricVehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestContainerConfig.class)
public class ElectricVehicleServiceTest {

  String DATA = "build_Test_EV_Data.sql";

  @Autowired private TestContainerConfig testContainerConfig;

  @Autowired private DataSource dataSource;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private ElectricVehicleService electricVehicleService;

  private void addTestData(String sqlFile) {
    testContainerConfig.loadData(dataSource, sqlFile);
  }

  @Test
  @Transactional
  public void canSaveAndUpdateEV() {
    ElectricVehicle ev = new ElectricVehicle();
    ev.setBrand("Tesla");
    ev.setModel("Model 3");
    ev.setBatteryCapacity(50.0);
    ev.setPricePerMile(300.0);
    ev.setTopSpeed(150.0);
    ev.setEvRange(450.0);
    ev.setEfficiency(300.0);
    ev.setRapidChargeSpeed(100.0);
    ev.setChargePortTypes(List.of(ConnectionType.TYPE2, ConnectionType.CCS));

    Optional<ElectricVehicle> savedEV = electricVehicleService.saveElectricVehicle(ev);

    assertTrue(savedEV.isPresent());
    assertEquals("Tesla", savedEV.get().getBrand());

    ev.setBatteryCapacity(60.0);
    Optional<ElectricVehicle> updatedEV = electricVehicleService.saveElectricVehicle(ev);
    assertTrue(updatedEV.isPresent());
    assertEquals(updatedEV.get().getId(), updatedEV.get().getId());
    assertEquals(60.0, updatedEV.get().getBatteryCapacity());
  }

  @Test
  @Transactional
  public void canFindAllVehiclesCompact() {

    addTestData(DATA);
    List<ElectricVehicleDto> evs = electricVehicleService.findAllVehiclesCompact();
    assertTrue(evs.size() > 200);
  }
}
