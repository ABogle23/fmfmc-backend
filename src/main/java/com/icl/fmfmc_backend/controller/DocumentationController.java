package com.icl.fmfmc_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

/** REST controller for serving API documentation. */
@RestController
@RequestMapping("/api")
public class DocumentationController {

  /**
   * Endpoint to fetch the OpenAPI documentation in YAML format.
   *
   * @return ResponseEntity containing the YAML documentation.
   */
  @GetMapping("/api-docs-yaml")
  public ResponseEntity<String> showOpenApiYaml() {
    String openapiUrl = "http://localhost:8080/v3/api-docs.yaml"; // default yaml endpoint
    RestTemplate restTemplate = new RestTemplate();
    String yamlDocs = restTemplate.getForObject(openapiUrl, String.class);

    System.out.println(yamlDocs);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "text/yaml; charset=UTF-8")
        .body(yamlDocs);
  }

  /**
   * Endpoint to fetch the OpenAPI documentation in JSON format.
   *
   * @return ResponseEntity containing the JSON documentation.
   */
  @GetMapping("/api-docs-json")
  public ResponseEntity<String> showOpenApiJson() {
    String openapiUrl = "http://localhost:8080/v3/api-docs"; // default json endpoint
    RestTemplate restTemplate = new RestTemplate();
    String jsonDocs = restTemplate.getForObject(openapiUrl, String.class);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
        .body(jsonDocs);
  }
}
