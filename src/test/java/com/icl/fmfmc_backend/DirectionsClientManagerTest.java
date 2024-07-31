package com.icl.fmfmc_backend;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.icl.fmfmc_backend.integration.DirectionsClient;
import com.icl.fmfmc_backend.dto.Routing.DirectionsRequest;
import com.icl.fmfmc_backend.dto.Routing.DirectionsResponse;
import com.icl.fmfmc_backend.exception.BadRequestException;
import com.icl.fmfmc_backend.exception.DirectionsClientException;
import com.icl.fmfmc_backend.exception.ServiceUnavailableException;
import com.icl.fmfmc_backend.service.DirectionsClientManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DirectionsClientManagerTest {

  private DirectionsClient osrClient;
  private DirectionsClient mapboxClient;
  private DirectionsClientManager directionsClientManager;

  @BeforeEach
  public void setUp() {
    osrClient = Mockito.mock(DirectionsClient.class);
    mapboxClient = Mockito.mock(DirectionsClient.class);
    directionsClientManager = new DirectionsClientManager(osrClient, mapboxClient);
  }

  @Test
  public void fetchDirectionsSuccessfully() throws DirectionsClientException {
    DirectionsRequest request = new DirectionsRequest();
    DirectionsResponse response = new DirectionsResponse();
    when(osrClient.getDirections(request)).thenReturn(response);

    DirectionsResponse result = directionsClientManager.getDirections(request);

    assertEquals(response, result);
    verify(osrClient, times(1)).getDirections(request);
  }

  @Test
  public void switchToMapboxOnServiceUnavailableException() throws DirectionsClientException {
    DirectionsRequest request = new DirectionsRequest();
    when(osrClient.getDirections(request))
        .thenThrow(new ServiceUnavailableException("Service Unavailable"));
    DirectionsResponse response = new DirectionsResponse();
    when(mapboxClient.getDirections(request)).thenReturn(response);

    DirectionsResponse result = directionsClientManager.getDirections(request);

    assertEquals(response, result);
    verify(osrClient, times(1)).getDirections(request);
    verify(mapboxClient, times(1)).getDirections(request);
  }

  @Test
  public void throwDirectionsClientExceptionOnBadRequest() {
    DirectionsRequest request = new DirectionsRequest();
    when(osrClient.getDirections(request)).thenThrow(new BadRequestException("Bad Request"));

    assertThrows(
        DirectionsClientException.class, () -> directionsClientManager.getDirections(request));
    verify(osrClient, times(1)).getDirections(request);
  }

  @Test
  public void throwRuntimeExceptionOnGenericException() {
    DirectionsRequest request = new DirectionsRequest();
    when(osrClient.getDirections(request)).thenThrow(new RuntimeException("Runtime Exception"));

    assertThrows(RuntimeException.class, () -> directionsClientManager.getDirections(request));
    verify(osrClient, times(1)).getDirections(request);
  }

  @Test
  public void invalidClientNameDoesNotSwitch() {
    directionsClientManager.setClient("invalid");

    assertEquals("osr", directionsClientManager.getActiveClientName());
  }
}
