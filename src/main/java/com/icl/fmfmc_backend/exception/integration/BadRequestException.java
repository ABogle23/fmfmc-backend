package com.icl.fmfmc_backend.exception.integration;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
