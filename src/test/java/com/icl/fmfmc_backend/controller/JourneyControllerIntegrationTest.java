package com.icl.fmfmc_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icl.fmfmc_backend.dto.api.JourneyContext;
import com.icl.fmfmc_backend.dto.api.RouteRequest;
import com.icl.fmfmc_backend.dto.api.RouteResult;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import com.icl.fmfmc_backend.service.EvScraperService;
import com.icl.fmfmc_backend.service.JourneyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(controllers = JourneyController.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//@Import(JourneyControllerIntegrationTest.MockSecurityConfig.class)
public class JourneyControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JourneyService journeyService;

  @MockBean private EvScraperService evScraperService;

  @MockBean private ChargerUpdateScheduler chargerUpdateScheduler;

  @Autowired private ObjectMapper objectMapper;

//  @TestConfiguration
//  static class MockSecurityConfig {
//    @Bean
//    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//      http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
//          .csrf(csrf -> csrf.disable());
//      return http.build();
//    }
//  }

  @Test
  public void minValidRequestParamsReturnsSuccess() throws Exception {
    RouteRequest validRequest = new RouteRequest();
    validRequest.setStartLat(34.0522);
    validRequest.setStartLong(-118.2437);
    validRequest.setEndLat(34.0522);
    validRequest.setEndLong(-118.2437);

    RouteResult expectedRouteResult = new RouteResult();
    when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
        .thenReturn(expectedRouteResult);

    mockMvc
        .perform(
            post("/api/find-route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  public void allValidParamsRequestParamsReturnsSuccess() throws Exception {
    String jsonPayload =
        """
        {
          "start_lat": 34.0522,
          "start_long": -118.2437,
          "end_lat": 34.0522,
          "end_long": -118.2437,
          "starting_battery": 0.8,
          "ev_range": 200000,
          "battery_capacity": 100.0,
          "min_charge_level": 0.2,
          "max_charge_level": 0.8,
          "charge_level_after_each_stop": 0.8,
          "final_destination_charge_level": 0.8,
          "connection_types": ["type1","type2"],
          "access_types": ["public"],
          "min_kw_charge_speed": 50,
          "max_kw_charge_speed": 150,
          "min_no_charge_points": 2,
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

    RouteRequest validRouteRequest = objectMapper.readValue(jsonPayload, RouteRequest.class);

    RouteResult expectedRouteResult = new RouteResult();
    when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
        .thenReturn(expectedRouteResult);

    System.out.println("Json Payload:\n" + jsonPayload);

    mockMvc
        .perform(
            post("/api/find-route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRouteRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(print());
  }

  /*************************
   *  Exception Tests  *
   *************************/

  @Test
  public void validationFailureReturnsBadRequest() throws Exception {
    RouteRequest invalidRequest = new RouteRequest();

    mockMvc
        .perform(
            post("/api/find-route")
                .contentType(MediaType.APPLICATION_JSON)
                //                        .header("X-API-Key", fmfmcApiKeyProperties.getApiKey())
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation Failed"));
  }

  @Test
  public void journeyNotFoundExceptionReturnsNotFound() throws Exception {
    RouteRequest validRequest = new RouteRequest();
    validRequest.setStartLat(34.0522);
    validRequest.setStartLong(-118.2437);
    validRequest.setEndLat(34.0522);
    validRequest.setEndLong(-118.2437);

    when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
        .thenThrow(new JourneyNotFoundException("No valid journey found."));

    mockMvc
        .perform(
            post("/api/find-route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.message").value("Please check your input parameters and try again."));
  }

  @Test
  public void validationFailureReturnsBadRequestAndReason() throws Exception {
    String jsonPayload =
        """
            {
              "start_lat": 34.0522,
              "start_long": -118.2437,
              "end_lat": 34.0522,
              "end_long": -118.2437,
              "eating_option_search_deviation": "INVALID"
            }
            """;

    RouteResult expectedRouteResult = new RouteResult();
    when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
        .thenReturn(expectedRouteResult);

    mockMvc
        .perform(
            post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.details[0]")
                .value("eating_option_search_deviation" + ": Invalid input provided."))
        .andDo(print());
  }

  /*************************
   *  Parameterized Tests  *
   *************************/

  public static final Double DEFAULT_START_LAT = 34.0522;

  public static final Double DEFAULT_START_LONG = -118.2437;
  public static final Double DEFAULT_END_LAT = 34.0522;
  public static final Double DEFAULT_END_LONG = -118.2437;

  public static String createRouteRequestJson(
      Double startLat,
      Double startLong,
      Double endLat,
      Double endLong,
      Map<String, Object> additionalParams) {
    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append(
        String.format(
            """
        {
          "start_lat": %s,
          "start_long": %s,
          "end_lat": %s,
          "end_long": %s
        """,
            startLat, startLong, endLat, endLong));

    additionalParams.forEach(
        (key, value) -> {
          jsonBuilder.append(String.format(", \"%s\": %s", key, value.toString()));
        });

    jsonBuilder.append("}");
    return jsonBuilder.toString();
  }

  public static String createRouteRequestJsonWithDefaults(Map<String, Object> additionalParams) {
    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append(
        String.format(
            """
                    {
                      "start_lat": %s,
                      "start_long": %s,
                      "end_lat": %s,
                      "end_long": %s
                    """,
            DEFAULT_START_LAT, DEFAULT_START_LONG, DEFAULT_END_LAT, DEFAULT_END_LONG));

    additionalParams.forEach(
        (key, value) -> {
          jsonBuilder.append(String.format(", \"%s\": %s", key, value.toString()));
        });

    jsonBuilder.append("}");
    return jsonBuilder.toString();
  }

  private static Stream<Arguments> routeRequestProvider() {
    return Stream.of(
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("starting_battery", 0.8)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("starting_battery", -0.1)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("starting_battery", 1.1)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("ev_range", 2000)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("ev_range", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("battery_capacity", 2000)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("battery_capacity", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_charge_level", 0.8)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_charge_level", -0.1)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_charge_level", 1.1)), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("charge_level_after_each_stop", 0.8)), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("charge_level_after_each_stop", -0.1)),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("charge_level_after_each_stop", 1.1)), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("final_destination_charge_level", 0.8)),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("final_destination_charge_level", -0.1)),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("final_destination_charge_level", 0.95)),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("final_destination_charge_level", 0.6, "charge_level_after_each_stop", 0.8)),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of(
                    "final_destination_charge_level", 0.95, "charge_level_after_each_stop", 0.8)),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("connection_types", "[\"type1\",\"type2\"]")),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("connection_types", "[\"INVALID\",\"type2\"]")),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("access_types", "[\"public\",\"restricted\"]")),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("access_types", "[\"INVALID\",\"public\"]")),
            false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_kw_charge_speed", -1)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_kw_charge_speed", 250)), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("min_kw_charge_speed", 1000)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_kw_charge_speed", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_kw_charge_speed", 250)), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("max_kw_charge_speed", 1000)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_no_charge_points", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_no_charge_points", 2)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("stop_for_eating", true)), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("stop_for_eating", "invalid")), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("eating_options", "[\"greek_restaurant\",\"cafe\"]")),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("eating_options", "[\"INVALID\",\"cafe\"]")),
            false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_price", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_price", 2)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("min_price", 5)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_price", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_price", 2)), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_price", 5)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_walking_distance", 0)), false),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("max_walking_distance", 250)), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("max_walking_distance", 100000)), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("include_alternative_Eating_options", true)),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("include_alternative_Eating_options", "invalid")),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("depart_time", "\"14:46:00\"")), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("depart_time", "46:243:32.5000")), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("break_duration", "\"14:46:00\"")), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("break_duration", "1")), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("break_duration", "\"01:00:00.5\"")), true),
        Arguments.of(createRouteRequestJsonWithDefaults(Map.of("break_duration", "1")), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("stopping_range", "\"middle\"")), true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("stopping_range", "INVALID")), false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("charger_search_deviation", "\"minimal\"")),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(Map.of("charger_search_deviation", "INVALID")),
            false),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("eating_option_search_deviation", "\"minimal\"")),
            true),
        Arguments.of(
            createRouteRequestJsonWithDefaults(
                Map.of("eating_option_search_deviation", "\"INVALID\"")),
            false));
  }

  @ParameterizedTest
  @MethodSource("routeRequestProvider")
  public void testRouteRequestSerialization(String jsonPayload, boolean isSuccessExpected)
      throws Exception {

    if (isSuccessExpected) {
      RouteRequest routeRequest = objectMapper.readValue(jsonPayload, RouteRequest.class);
      when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
          .thenReturn(new RouteResult());

      mockMvc
          .perform(
              post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));

      verify(journeyService, times(1))
          .getJourney(any(RouteRequest.class), any(JourneyContext.class));
    } else {
      mockMvc
          .perform(
              post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Validation Failed"));

      verify(journeyService, never())
          .getJourney(any(RouteRequest.class), any(JourneyContext.class));
    }
  }
}
