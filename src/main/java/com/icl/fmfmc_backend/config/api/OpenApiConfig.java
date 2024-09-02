package com.icl.fmfmc_backend.config.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for OpenAPI documentation. */
@Configuration
public class OpenApiConfig {

  /**
   * Bean definition for customizing the OpenAPI documentation.
   *
   * @return an OpenAPI object with custom information
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Feed me Feed my Car API")
                .version("1.0")
                .description("API documentation for Feed me Feed my Car."));
  }
}
