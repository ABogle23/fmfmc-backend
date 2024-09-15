package com.icl.fmfmc_backend.integration;

import com.icl.fmfmc_backend.dto.directions.DirectionsRequest;
import com.icl.fmfmc_backend.dto.directions.DirectionsResponse;
import com.icl.fmfmc_backend.config.integration.MapboxProperties;
import com.icl.fmfmc_backend.exception.integration.BadRequestException;
import com.icl.fmfmc_backend.exception.api.GenericApiException;
import com.icl.fmfmc_backend.exception.integration.ServiceUnavailableException;
import com.icl.fmfmc_backend.integration.directions.MapboxDirectionsClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class MapboxDirectionsClientTest {
  private MockWebServer mockWebServer;
  private MapboxDirectionsClient client;
  private MapboxProperties properties;

  @BeforeEach
  public void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    properties = new MapboxProperties();
    setField(properties, "mapboxDirectionsServiceBaseUrl", mockWebServer.url("/").toString());
    setField(properties, "apiKey", "test-api-key");
    client = new MapboxDirectionsClient(properties);
  }

  @AfterEach
  public void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  private void setField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void getDirectionsSuccessfully() {
    mockWebServer.enqueue(
        new MockResponse()
            .setBody(
                "{\"routes\":[{\"geometry\":{\"coordinates\":[[0.0,0.0],[1.0,1.0]]},\"distance\":100,\"duration\":10,\"legs\":[{\"distance\":50,\"duration\":5}]}]}")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-mapbox-routing-engine", "v2.0"));

    DirectionsRequest request = new DirectionsRequest();
    request.setCoordinates(Arrays.asList(new Double[] {0.0, 0.0}, new Double[] {1.0, 1.0}));

    DirectionsResponse response = client.getDirections(request);

    assertEquals(100, response.getTotalDistance());
    assertEquals(10, response.getTotalDuration());
    assertNotNull(response.getLineString());
    assertEquals(1, response.getLegDurations().size());
    assertEquals(5, response.getLegDurations().get(0));
    assertEquals(1, response.getLegDistances().size());
    assertEquals(50, response.getLegDistances().get(0));
  }

  @Test
  public void getDirectionsWithInvalidApiKeyThrowsGenericApiException() {
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(401)
            .setBody("{\"message\":\"Invalid Api Key\"}")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-mapbox-routing-engine", "v2.0"));

    DirectionsRequest request = new DirectionsRequest();
    request.setCoordinates(Arrays.asList(new Double[] {0.0, 0.0}, new Double[] {1.0, 1.0}));

    assertThrows(GenericApiException.class, () -> client.getDirections(request));
  }

  @Test
  public void getDirectionsWithServerErrorThrowsServiceUnavailableException() {
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setBody("{\"message\":\"Internal Server Error\"}")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-mapbox-routing-engine", "v2.0"));

    DirectionsRequest request = new DirectionsRequest();
    request.setCoordinates(Arrays.asList(new Double[] {0.0, 0.0}, new Double[] {1.0, 1.0}));

    assertThrows(ServiceUnavailableException.class, () -> client.getDirections(request));
  }

  @Test
  public void getDirectionsWithServerErrorThrowsBadRequestException() {
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody("{\"message\":\"Bad Request\"}")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-mapbox-routing-engine", "v2.0"));

    DirectionsRequest request = new DirectionsRequest();
    request.setCoordinates(Arrays.asList(new Double[] {0.0, 0.0}, new Double[] {1.0, 1.0}));

    assertThrows(BadRequestException.class, () -> client.getDirections(request));
  }
}
