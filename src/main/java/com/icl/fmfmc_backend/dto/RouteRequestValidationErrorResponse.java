package com.icl.fmfmc_backend.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;


// used for returning error response in case of validation
// errors on the Post object in /find-route endpoint@Data
@RequiredArgsConstructor
public class RouteRequestValidationErrorResponse {

  private final String field;
  private final String message;
}
