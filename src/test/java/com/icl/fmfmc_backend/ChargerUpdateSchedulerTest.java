package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.Integration.OpenChargeMapClient;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ChargerUpdateSchedulerTest {

  @MockBean private ChargerService chargerService;

  @MockBean private OpenChargeMapClient openChargeMapClient;

  @Autowired private ChargerUpdateScheduler chargerUpdateScheduler;

  private List<Charger> prepareMockChargers() {
    Charger charger1 = new Charger();
    Charger charger2 = new Charger();
    return Arrays.asList(charger1, charger2);
  }

  @Test
  public void testUpdateChargers() {
    List<Charger> mockChargers = prepareMockChargers();

    doAnswer(
            invocation -> {
              //            MultiValueMap<String, String> params = invocation.getArgument(0,
              // MultiValueMap.class);
              chargerService.saveChargersInBatch(mockChargers);
              return null;
            })
        .when(openChargeMapClient)
        .getChargerFromOpenChargeMapApi(any());

    chargerUpdateScheduler.updateChargers();

    verify(chargerService, times(6)).saveChargersInBatch(mockChargers);
    verify(chargerService).updateNullConnectionPowerKW();
    verify(openChargeMapClient, times(6)).getChargerFromOpenChargeMapApi(any());
    verifyNoMoreInteractions(chargerService, openChargeMapClient);
  }
}