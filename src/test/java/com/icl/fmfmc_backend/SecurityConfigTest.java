package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.config.FmfmcApiKeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private WebApplicationContext context;

  @Autowired private FmfmcApiKeyProperties fmfmcApiKeyProperties;

  @BeforeEach
  public void setup() {
    // Setup MockMvc to include security settings using the application context
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(
                SecurityMockMvcConfigurers
                    .springSecurity()) // Correct method to apply security configuration
            .build();
  }

  @Test
  public void shouldRejectUnauthenticatedAccess() throws Exception {
    mockMvc
        .perform(post("/api/find-route").secure(true))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser()
  public void shouldAllowAuthenticatedAccess() throws Exception {
    mockMvc
        .perform(post("/api/find-route").secure(true))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldAllowAuthenticatedAccessForValidApiKey() throws Exception {
    mockMvc
            .perform(post("/api/find-route")
                    .secure(true)
                    .header("X-Api-Key", fmfmcApiKeyProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldDisallowAuthenticatedAccessForInvalidApiKey() throws Exception {
    mockMvc
            .perform(post("/api/find-route")
                    .secure(true)
                    .header("X-Api-Key", "123")
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser()
  public void shouldEnforceHttps() throws Exception {
    mockMvc.perform(post("/api/find-route")).andDo(print()).andExpect(status().is3xxRedirection());
  }

  @Test
  public void shouldSetSecurityHeaders() throws Exception {
    mockMvc
        .perform(post("/api/any").secure(true))
        .andDo(print())
        .andExpect(header().string("X-Frame-Options", "DENY"))
        .andExpect(
            header().string("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("X-XSS-Protection", "0"))
        .andExpect(
            header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
        .andExpect(header().string("Pragma", "no-cache"))
        .andExpect(header().string("Expires", "0"));
  }
}
