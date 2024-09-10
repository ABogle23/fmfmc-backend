package com.icl.fmfmc_backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Successful response", content = @Content),
//        @ApiResponse(responseCode = "400", description = "Bad Request - The request could not be understood and therefore could not be processed", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failed", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found - Nothing matching the request was found", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests - Number of allowed requests exceeded", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - An unexpected error was encountered", content = @Content)
})
public @interface DocsResponseCodes {
}
