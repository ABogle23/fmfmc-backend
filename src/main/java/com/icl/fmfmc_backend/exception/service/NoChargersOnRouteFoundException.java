package com.icl.fmfmc_backend.exception.service;

public class NoChargersOnRouteFoundException extends Exception {

  public NoChargersOnRouteFoundException() {
    super("No chargers on route could be found.");
  }

  public NoChargersOnRouteFoundException(String message) {
    super(message);
  }

  public NoChargersOnRouteFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoChargersOnRouteFoundException(Throwable cause) {
    super(cause);
  }
}
