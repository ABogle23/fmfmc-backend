package com.icl.fmfmc_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.icl.fmfmc_backend.config.FmfmcApiKeyProperties;
import com.icl.fmfmc_backend.controller.JourneyController;
import com.icl.fmfmc_backend.dto.Api.JourneyContext;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import com.icl.fmfmc_backend.entity.enums.EnumUtils;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import com.icl.fmfmc_backend.exception.JourneyNotFoundException;
import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import com.icl.fmfmc_backend.service.EvScraperService;
import com.icl.fmfmc_backend.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// @WebMvcTest(controllers = JourneyController.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JourneyControllerIntegrationTest.MockSecurityConfig.class)
public class JourneyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JourneyService journeyService;

    @MockBean
    private EvScraperService evScraperService;

    @MockBean
    private ChargerUpdateScheduler chargerUpdateScheduler;

    FmfmcApiKeyProperties fmfmcApiKeyProperties;

    @Autowired
    private ObjectMapper objectMapper;
    @TestConfiguration
    static class MockSecurityConfig {
        @Bean
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                    .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }
//    @BeforeEach
//    public void setUp() {
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//
//    }


    @Test
    public void testGetJourneyReturnsSuccess() throws Exception {
        String jsonPayload = """
        {
          "startLat": 34.0522,
          "startLong": -118.2437,
          "endLat": 34.0522,
          "endLong": -118.2437,
          "startingBattery": 0.8,
          "evRange": 200000,
          "stopForEating": false,
          "eatingOptions": ["greekRestaurant"],
          "minPrice": 4
        }
        """;

        RouteResult expectedRouteResult = new RouteResult();
        when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
                .thenReturn(expectedRouteResult);

        mockMvc.perform(post("/api/find-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println(expectedRouteResult);

    }

    @Test
    public void testGetJourneyReturnsSuccessver2() throws Exception {
        RouteRequest validRequest = new RouteRequest();
        validRequest.setStartLat(34.0522);
        validRequest.setStartLong(-118.2437);
        validRequest.setEndLat(34.0522);
        validRequest.setEndLong(-118.2437);

        RouteResult expectedRouteResult = new RouteResult();
        when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
                .thenReturn(expectedRouteResult);

        mockMvc.perform(post("/api/find-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    public void testGetJourneyReturnsSuccessver3() throws Exception {
        RouteRequest validRequest = new RouteRequest();
        validRequest.setStartLat(34.0522);
        validRequest.setStartLong(-118.2437);
        validRequest.setEndLat(34.0522);
        validRequest.setEndLong(-118.2437);

        RouteResult expectedRouteResult = new RouteResult();
        when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
                .thenReturn(expectedRouteResult);

        String jsonPayload = objectMapper.writeValueAsString(validRequest);

        System.out.println("object mapper");
        System.out.println(jsonPayload);

        mockMvc.perform(post("/api/find-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(print()); ;
    }



    @Test
    public void testGetJourneyValidationFailure() throws Exception {
        RouteRequest invalidRequest = new RouteRequest();

//        System.out.println("Api Key" + fmfmcApiKeyProperties.getApiKey());

        mockMvc.perform(post("/api/find-route")
                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("X-API-Key", fmfmcApiKeyProperties.getApiKey())
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    public void testGetJourneyNotFound() throws Exception {
        RouteRequest validRequest = new RouteRequest();
        validRequest.setStartLat(34.0522);
        validRequest.setStartLong(-118.2437);
        validRequest.setEndLat(34.0522);
        validRequest.setEndLong(-118.2437);

        when(journeyService.getJourney(any(RouteRequest.class), any(JourneyContext.class)))
                .thenThrow(new JourneyNotFoundException("No valid journey found."));

        System.out.println("object mapper");
        System.out.println(objectMapper.writeValueAsString(validRequest));
        System.out.println(validRequest.getEatingOptions().get(0).getApiName());

        mockMvc.perform(post("/api/find-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Please check your input parameters and try again."));
    }




}