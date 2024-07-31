package com.icl.fmfmc_backend.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import org.jsoup.Jsoup;

public class CommonResponseHandler {

  private static final Logger logger = LoggerFactory.getLogger(CommonResponseHandler.class);

  public static <T> Mono<T> handleResponse(ClientResponse response, Class<T> clazz) {
    Mono<String> responseBody = response.bodyToMono(String.class);
    System.out.println("New response detected");

    if (response.statusCode().isError()) {
      return handleErrorResponse(response);
    }
//    else if (isMapboxResponse(response)) {
//      System.out.println("Mapbox response detected");
//      return responseBody.flatMap(
//          body -> {
//            if (body.contains("\"code\":\"NoSegment\"")
//                || body.contains("\"code\":\"NoRoute\"")) {
//              logger.error("Mapbox specific error: No valid segment/route found");
//              return Mono.error(
//                  new BadRequestException("Mapbox specific error: No valid segment/route found"));
//            }
//            System.out.println("Mapbox response is not an error");
//            return response.bodyToMono(clazz);
//          });
//    }
    return response.bodyToMono(clazz);
  }

  private static Mono handleErrorResponse(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(
            body -> {
              String cleanBody = cleanResponseBody(body);
              HttpStatusCode status = response.statusCode();
              int statusCode = status.value();
              logger.error("API Error: Status {}, Body {}", status, cleanBody);
              // throw specific exception based on the status
              switch (statusCode) {
                case 500, 503:
                  logger.error("Service Unavailable: {}", cleanBody);
                  return Mono.error(
                      new ServiceUnavailableException("Service Unavailable: " + cleanBody));
                case 400, 404, 422:
                  logger.error("Bad Request: {}", cleanBody);
                  return Mono.error(new BadRequestException("Bad Request: " + cleanBody));
                default:
                  logger.error("API error: {} {}", status, cleanBody);
                  return Mono.error(
                      new GenericApiException("API error: " + status + " " + cleanBody, status));
              }
            });
  }

  // summarise html content in the response body
  private static String cleanResponseBody(String body) {
    if (body != null && body.contains("<html>")) {
      // Convert HTML content to plain text to simplify logs
      return Jsoup.parse(body).text();
    }
    return body;
  }

  private static boolean isMapboxResponse(ClientResponse response) {
    HttpHeaders headers = response.headers().asHttpHeaders();
    return headers.entrySet().stream()
        .anyMatch(
            entry ->
                entry.getKey().toLowerCase().contains("mapbox")
                    || entry.getValue().stream()
                        .anyMatch(value -> value.toLowerCase().contains("mapbox")));
  }


}
