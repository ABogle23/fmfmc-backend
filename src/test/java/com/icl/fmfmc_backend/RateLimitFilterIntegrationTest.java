package com.icl.fmfmc_backend;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icl.fmfmc_backend.config.FilterConfig;
import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.security.RateLimitFilter;
import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import com.icl.fmfmc_backend.service.EvScraperService;
import com.icl.fmfmc_backend.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// @WebMvcTest(controllers = JourneyController.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import({
  RateLimitFilterIntegrationTest.MockSecurityConfig.class,
  FilterConfig.class,
  RateLimitFilter.class
})
public class RateLimitFilterIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JourneyService journeyService;

  @MockBean private EvScraperService evScraperService;

  @MockBean private ChargerUpdateScheduler chargerUpdateScheduler;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WebApplicationContext context;

  @Autowired private FilterConfig filterConfig;

  @Autowired private RateLimitFilter rateLimitFilter;

  @TestConfiguration
  static class MockSecurityConfig {
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
      http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
          .csrf(csrf -> csrf.disable())
          .requiresChannel(channel -> channel.anyRequest().requiresSecure()); // enforce https

      return http.build();
    }
  }

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(rateLimitFilter).build();
  }

  @Test
  public void rateLimitExceededShouldReturn429() throws Exception {
    RouteRequest validRequest = new RouteRequest();
    validRequest.setStartLat(34.0522);
    validRequest.setStartLong(-118.2437);
    validRequest.setEndLat(34.0522);
    validRequest.setEndLong(-118.2437);

    String jsonRequest = objectMapper.writeValueAsString(validRequest);
    int callCount = 20;

    for (int i = 0; i < callCount; i++) {
      mockMvc
          .perform(
              post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
          .andExpect(status().isOk()); // 200
    }

    mockMvc
        .perform(
            post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
        .andExpect(status().isTooManyRequests()); // 429
  }
}
