package com.icl.fmfmc_backend.stochasticTesting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icl.fmfmc_backend.Routing.GeometryService;
import com.icl.fmfmc_backend.util.JsonPayloadBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class StochasticRoutingTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private static List<Map<String, Object>> loadCityData() {
    try {
      var resource = new ClassPathResource("geonames-all-uk-cities-with-a-population-1000.json");
      return new ObjectMapper()
          .readValue(resource.getInputStream(), new TypeReference<List<Map<String, Object>>>() {});
    } catch (IOException e) {
      System.err.println("Error loading city data: " + e.getMessage());
      throw new RuntimeException("Failed to load city data", e);
    }
  }

  public static class RandomCityCoordinatesProvider implements ArgumentsProvider {
    private List<Map<String, Object>> cities = StochasticRoutingTest.loadCityData();

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      Random rand = new Random();
      return Stream.generate(
              () -> {
                Map<String, Object> startCity = cities.get(rand.nextInt(cities.size()));
                Map<String, Object> endCity = cities.get(rand.nextInt(cities.size()));

                Map<String, Double> startCoordinates =
                    (Map<String, Double>) startCity.get("coordinates");
                Map<String, Double> endCoordinates =
                    (Map<String, Double>) endCity.get("coordinates");

                if (startCoordinates == null || endCoordinates == null) {
                  throw new IllegalArgumentException("City coordinates are missing");
                }

                if (startCity.get("name").equals(endCity.get("name"))) {
                  throw new IllegalArgumentException("Cities are the same");
                }

                System.out.println(
                    "Start City: " + startCity.get("name") + ", End City: " + endCity.get("name"));

                double minChargeLevel = rand.nextDouble(0.1, 0.9);
                double chargeLevelAfterEachStop = rand.nextDouble(minChargeLevel, 0.9);
                double finalDestinationChargeLevel =
                    rand.nextDouble(minChargeLevel, chargeLevelAfterEachStop);

                return Arguments.of(
                    JsonPayloadBuilder.buildJsonPayload(
                        Map.of(
                            "start_lat", startCoordinates.get("lat"),
                            "start_long", startCoordinates.get("lon"),
                            "end_lat", endCoordinates.get("lat"),
                            "end_long", endCoordinates.get("lon"),
                            "ev_range", rand.nextInt(40000, 100000),
                            "starting_battery", rand.nextDouble(0.1, 1),
                            "min_charge_level", minChargeLevel,
                            "charge_level_after_each_stop", chargeLevelAfterEachStop,
                            "final_destination_charge_level", finalDestinationChargeLevel)));
              })
          .limit(10); // number of iterations
    }
  }

  public static class RandomCityCoordinatesProviderLongDistance implements ArgumentsProvider {
    private List<Map<String, Object>> cities = StochasticRoutingTest.loadCityData();

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      Random rand = new Random();
      return Stream.generate(
              () -> {
                Map<String, Object> startCity = null;
                Map<String, Object> endCity = null;
                Map<String, Double> startCoordinates = null;
                Map<String, Double> endCoordinates = null;
                do {
                  startCity = cities.get(rand.nextInt(cities.size()));
                  endCity = cities.get(rand.nextInt(cities.size()));

                  startCoordinates = (Map<String, Double>) startCity.get("coordinates");
                  endCoordinates = (Map<String, Double>) endCity.get("coordinates");

                } while (GeometryService.calculateDistanceBetweenPoints(
                        startCoordinates.get("lat"),
                        startCoordinates.get("lon"),
                        endCoordinates.get("lat"),
                        endCoordinates.get("lon"))
                    < 400000);

                if (startCoordinates == null || endCoordinates == null) {
                  throw new IllegalArgumentException("City coordinates are missing");
                }

                if (startCity.get("name").equals(endCity.get("name"))) {
                  throw new IllegalArgumentException("Cities are the same");
                }

                System.out.println(
                    "Start City: " + startCity.get("name") + ", End City: " + endCity.get("name"));

                double minChargeLevel = rand.nextDouble(0.1, 0.9);
                double chargeLevelAfterEachStop = rand.nextDouble(minChargeLevel, 0.9);
                double finalDestinationChargeLevel =
                    rand.nextDouble(minChargeLevel, chargeLevelAfterEachStop);

                return Arguments.of(
                    JsonPayloadBuilder.buildJsonPayload(
                        Map.of(
                            "start_lat", startCoordinates.get("lat"),
                            "start_long", startCoordinates.get("lon"),
                            "end_lat", endCoordinates.get("lat"),
                            "end_long", endCoordinates.get("lon"),
                            "ev_range", rand.nextInt(60000, 100000),
                            "starting_battery", rand.nextDouble(0.5, 1),
                            "min_charge_level", minChargeLevel,
                            "charge_level_after_each_stop", chargeLevelAfterEachStop,
                            "final_destination_charge_level", finalDestinationChargeLevel)));
              })
          .limit(10); // number of iterations
    }
  }

  @ParameterizedTest
  @ArgumentsSource(RandomCityCoordinatesProvider.class)
  @Tag("exclude")
  public void requestWithDynamicOptionsReturnsValidResult(String jsonPayload) throws Exception {
    System.out.println("Json Payload:\n" + jsonPayload);

    mockMvc
        .perform(
            post("/api/find-route").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
    //            .andDo(print());
  }
}
