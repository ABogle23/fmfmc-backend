package com.icl.fmfmc_backend.util;

public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String message) {
    super(message);
  }
}