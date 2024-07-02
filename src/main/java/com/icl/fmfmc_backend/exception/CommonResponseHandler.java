package com.icl.fmfmc_backend.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import org.jsoup.Jsoup;

public class CommonResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommonResponseHandler.class);

    public static <T> Mono<T> handleResponse(ClientResponse response, Class<T> clazz) {
        if (response.statusCode().isError()) {
            return handleErrorResponse(response);
        }
        return response.bodyToMono(clazz);
    }

    private static Mono handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            String cleanBody = cleanResponseBody(body);
            HttpStatusCode status = response.statusCode();
            int statusCode = status.value();
            // Log the error
            logger.error("API Error: Status {}, Body {}", status, cleanBody);
            // Throw a specific exception based on the status
            switch (statusCode) {
                case 503:
                    return Mono.error(new ServiceUnavailableException("Service Unavailable: " + cleanBody));
                case 400:
                    return Mono.error(new BadRequestException("Bad Request: " + cleanBody));
                default:
                    return Mono.error(new GenericApiException("API error: " + status + " " + cleanBody, status));
            }
        });
    }

    // Helper method to clean or summarize HTML content in the response body
    private static String cleanResponseBody(String body) {
        if (body != null && body.contains("<html>")) {
            // Convert HTML content to plain text to simplify logs
            return Jsoup.parse(body).text();
        }
        return body;
    }
}