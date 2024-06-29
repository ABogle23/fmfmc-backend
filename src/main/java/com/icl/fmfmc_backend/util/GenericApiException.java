package com.icl.fmfmc_backend.util;

import org.springframework.http.HttpStatusCode;

public class GenericApiException extends RuntimeException {
  private final HttpStatusCode status;

  public GenericApiException(String message, HttpStatusCode status) {
    super(message);
    this.status = status;
  }

  public HttpStatusCode getStatus() {
    return status;
  }
}