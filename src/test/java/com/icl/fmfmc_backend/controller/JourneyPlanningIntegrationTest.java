package com.icl.fmfmc_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icl.fmfmc_backend.config.TestContainerConfig;
import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.integration.directions.DirectionsClient;
import com.icl.fmfmc_backend.integration.foodEstablishment.FoodEstablishmentClient;
import com.icl.fmfmc_backend.util.JsonPayloadBuilder;
import com.icl.fmfmc_backend.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
// @ContextConfiguration(classes = {TestContainerConfig.class, TestDataLoaderConfig.class})
@ContextConfiguration(classes = {TestContainerConfig.class})
public class JourneyPlanningIntegrationTest {

  String HEAVY_DATA = "build_Test_DB_Data.sql";
  String LITE_DATA = "build_Test_DB_Data_lite.sql";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  //  @Autowired private JourneyService journeyService;

  @MockBean private DirectionsClient directionsClient;

  @MockBean private FoodEstablishmentClient foodEstablishmentClient;

  @Autowired private DataSource dataSource;

  @Autowired TestContainerConfig testContainerConfig;

  @Autowired private JdbcTemplate jdbcTemplate;

  private void addTestData(String sqlFile) {
    testContainerConfig.loadData(dataSource, sqlFile);
  }

  @BeforeEach
  public void setUp() {
    addTestData(HEAVY_DATA);
  }

  @AfterEach
  public void tearDown() {
    testContainerConfig.clearTables(jdbcTemplate);
  }

  @AfterEach
  public void resetMocks() {
    Mockito.reset(directionsClient, foodEstablishmentClient);
  }

  @Test
  public void requestWithoutEatingOptionReturnsValidResult() throws Exception {
    String jsonPayload =
        """
                {
                  "start_lat": 51.107673,
                  "start_long": -2.110748,
                  "end_lat": 51.460065,
                  "end_long": -1.117859,
                  "starting_battery": 0.9,
                  "ev_range": 100000,
                  "battery_capacity": 100.0,
                  "min_charge_level": 0.3,
                  "charge_level_after_each_stop": 0.9,
                  "final_destination_charge_level": 0.5,
                  "connection_types": null,
                  "access_types": null,
                  "min_kw_charge_speed": null,
                  "max_kw_charge_speed": null,
                  "min_no_charge_points": 1,
                  "stop_for_eating": false,
                  "min_price": 2,
                  "max_price": 4,
                  "max_walking_distance": 500,
                  "include_alternative_eating_options": false,
                  "depart_time": "14:46:24.700000",
                  "meal_time": "00:00:00.000000",
                  "break_duration": "00:01:00.000000",
                  "stopping_range": "middle",
                  "charger_search_deviation": "moderate",
                  "eating_option_search_deviation": "moderate"
                }
                """;

    DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
    Mockito.when(directionsClient.getDirections(any())).thenReturn(directionsResponse);

    List<FoodEstablishment> foodEstablishments =
        TestDataFactory.createFoodEstablishmentsForPoiTest();
    Mockito.when(foodEstablishmentClient.getFoodEstablishmentsByParam(any()))
        .thenReturn(foodEstablishments);

    JourneyRequest validJourneyRequest = objectMapper.readValue(jsonPayload, JourneyRequest.class);
    System.out.println("Json Payload:\n" + jsonPayload);

    mockMvc
        .perform(
            post("/api/find-journey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validJourneyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.chargers.length()").value(1))
        .andExpect(jsonPath("$.data.chargers[0].id").value(41030))
        .andDo(print());
  }

  @Test
  public void requestWithoutEatingOptionAndWithEnoughChargeReturnsValidResult() throws Exception {
    Map<String, Object> replacements = Map.of("ev_range", 300000.0);

    String jsonPayload = JsonPayloadBuilder.buildJsonPayload(replacements);

    DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
    Mockito.when(directionsClient.getDirections(any())).thenReturn(directionsResponse);

    List<FoodEstablishment> foodEstablishments =
        TestDataFactory.createFoodEstablishmentsForPoiTest();
    Mockito.when(foodEstablishmentClient.getFoodEstablishmentsByParam(any()))
        .thenReturn(foodEstablishments);

    JourneyRequest validJourneyRequest = objectMapper.readValue(jsonPayload, JourneyRequest.class);
    System.out.println("validRouteRequest:\n" + validJourneyRequest);
    System.out.println("Json Payload:\n" + jsonPayload);
    String converted = objectMapper.writeValueAsString(validJourneyRequest);
    System.out.println("Converted Back:\n" + converted);

    Integer expChargersCount = 0;
    Integer expSegmentCount = 1;

    mockMvc
        .perform(
            post("/api/find-journey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validJourneyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.chargers.length()").value(expChargersCount))
        .andExpect(
            jsonPath("$.data.segment_details.stop_durations.length()").value(expChargersCount))
        .andExpect(
            jsonPath("$.data.segment_details.charge_speeds_kw.length()").value(expChargersCount))
        .andExpect(
            jsonPath("$.data.segment_details.arrival_charges.length()").value(expSegmentCount))
        .andExpect(
            jsonPath("$.data.segment_details.departing_charges.length()").value(expSegmentCount))
        .andExpect(jsonPath("$.data.segment_details.arrival_times.length()").value(expSegmentCount))
        .andExpect(jsonPath("$.data.segment_details.depart_times.length()").value(expSegmentCount))
        .andExpect(
            jsonPath("$.data.segment_details.segment_distances.length()").value(expSegmentCount))
        .andExpect(
            jsonPath("$.data.segment_details.segment_durations.length()").value(expSegmentCount))
        .andDo(print());
  }

  @Test
  public void requestWithEatingOptionReturnsValidResultWithMultipleChargers() throws Exception {
    Map<String, Object> replacements =
        Map.of(
            "stop_for_eating",
            true,
            "starting_battery",
            0.9,
            "min_charge_level",
            0.2,
            "charge_level_after_each_stop",
            0.9,
            "final_destination_charge_level",
            0.2,
            "charger_search_deviation",
            "moderate",
            "eating_option_search_deviation",
            "moderate");

    String jsonPayload = JsonPayloadBuilder.buildJsonPayload(replacements);

    DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
    Mockito.when(directionsClient.getDirections(any()))
        .thenReturn(directionsResponse)
        .thenReturn(TestDataFactory.createSnappedDirectionsResponse())
        .thenReturn(TestDataFactory.createFinalDirectionsResponse());

    List<FoodEstablishment> foodEstablishments =
        TestDataFactory.createFoodEstablishmentsForPoiTest();
    FoodEstablishment optimalFe =
        TestDataFactory.createDefaultFoodEstablishment("999", "OptimalFe", -1.480544, 51.206458);
    optimalFe.setPopularity(0.99);
    optimalFe.setRating(9.9);
    foodEstablishments.add(optimalFe);

    for (FoodEstablishment fe : foodEstablishments) {
      System.out.println("Food Establishment: " + fe.getName());
    }

    Mockito.when(foodEstablishmentClient.getFoodEstablishmentsByParam(any()))
        .thenReturn(foodEstablishments);

    JourneyRequest validJourneyRequest = objectMapper.readValue(jsonPayload, JourneyRequest.class);
    System.out.println("Json Payload:\n" + jsonPayload);

    mockMvc
        .perform(
            post("/api/find-journey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validJourneyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.chargers.length()").value(1))
        .andExpect(jsonPath("$.data.chargers[0].id").value(148485))
        .andDo(print());
  }

  /*************************
   *  Exception Tests  *
   *************************/

  /*************************
   *  Parameterized Tests  *
   *************************/

  public static Stream<Arguments> routeRequestTestData() {
    return Stream.of(
        Arguments.of(
            JsonPayloadBuilder.buildJsonPayload(
                Map.of("stop_for_eating", true, "starting_battery", 0.9, "ev_range", 100000)),
            new ResponseExpectations(2, "Route successfully calculated.", null, null),
            true),
        Arguments.of(
            JsonPayloadBuilder.buildJsonPayload(
                Map.of("stop_for_eating", false, "starting_battery", 0.8)),
            new ResponseExpectations(2, "Route successfully calculated.", null, null),
            true));
  }

  public static class ResponseExpectations {
    Integer expectedChargersCount;
    String message;
    Double distance;
    List<String> eatingOptions;

    public ResponseExpectations(
        Integer expectedChargersCount,
        String message,
        Double distance,
        List<String> eatingOptions) {
      this.expectedChargersCount = expectedChargersCount;
      this.message = message;
      this.distance = distance;
      this.eatingOptions = eatingOptions != null ? eatingOptions : Collections.emptyList();
    }
  }

  @ParameterizedTest
  @MethodSource("routeRequestTestData")
  public void requestWithDynamicOptionsReturnsValidResult(
      String jsonPayload, ResponseExpectations expectations, boolean isSuccessExpected)
      throws Exception {

    DirectionsResponse directionsResponse = TestDataFactory.createDefaultDirectionsResponse();
    Mockito.when(directionsClient.getDirections(any()))
        .thenReturn(directionsResponse)
        .thenReturn(TestDataFactory.createSnappedDirectionsResponse())
        .thenReturn(TestDataFactory.createFinalDirectionsResponse());

    List<FoodEstablishment> foodEstablishments =
        TestDataFactory.createFoodEstablishmentsForPoiTest();

    FoodEstablishment optimalFe =
        TestDataFactory.createDefaultFoodEstablishment(
            "999", "OptimalFe", -1.5353251185417531, 51.21390363023607);
    optimalFe.setPopularity(0.99);
    optimalFe.setRating(9.9);
    foodEstablishments.add(optimalFe);

    Mockito.when(foodEstablishmentClient.getFoodEstablishmentsByParam(any()))
        .thenReturn(foodEstablishments);

    System.out.println("Json Payload:\n" + jsonPayload);

    if (!isSuccessExpected) {
      mockMvc
          .perform(
              post("/api/find-journey").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
          .andExpect(status().isNotFound())
          .andDo(print());

    } else {

      ResultActions resultActions =
          mockMvc
              .perform(
                  post("/api/find-journey")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(jsonPayload))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andDo(print());

      if (expectations.message != null) {
        resultActions.andExpect(jsonPath("$.message").value(expectations.message));
      }

      if (expectations.distance != null) {
        //        resultActions.andExpect(jsonPath("$.data.distance").value(expectations.distance));
      }

      if (expectations.expectedChargersCount != null) {
        resultActions.andExpect(
            jsonPath("$.data.chargers.length()").value(expectations.expectedChargersCount));
      }

      if (!expectations.eatingOptions.isEmpty()) {
        //        resultActions.andExpect(
        //            jsonPath(
        //                "$.data.eating_options",
        //                hasItems(expectations.eatingOptions.toArray(new String[0]))));
      }
    }
  }
}
