package com.icl.fmfmc_backend.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.icl.fmfmc_backend.dto.api.JourneyContext;
import com.icl.fmfmc_backend.dto.api.JourneyRequest;
import com.icl.fmfmc_backend.dto.api.JourneyResult;
import com.icl.fmfmc_backend.exception.service.JourneyNotFoundException;
import com.icl.fmfmc_backend.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class JourneyControllerTest {

  @Mock private JourneyService journeyService;

  @InjectMocks private JourneyController journeyController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getJourneyReturnsSuccess() throws JourneyNotFoundException {
    JourneyRequest journeyRequest = new JourneyRequest();
    JourneyResult expectedJourneyResult = new JourneyResult();
    when(journeyService.getJourney(any(JourneyRequest.class), any(JourneyContext.class)))
        .thenReturn(expectedJourneyResult);

    ResponseEntity<?> response = journeyController.getJourney(journeyRequest);
    assertEquals("Response status is OK", HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void getJourneyThrowsJourneyNotFoundException() throws JourneyNotFoundException {
    JourneyRequest journeyRequest = new JourneyRequest(); // setup
    when(journeyService.getJourney(any(JourneyRequest.class), any(JourneyContext.class)))
        .thenThrow(new JourneyNotFoundException("Not found"));

    assertThrows(JourneyNotFoundException.class, () -> journeyController.getJourney(journeyRequest));
  }
}
