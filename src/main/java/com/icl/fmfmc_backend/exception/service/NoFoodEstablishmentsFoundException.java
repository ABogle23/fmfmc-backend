package com.icl.fmfmc_backend.exception.service;

public class NoFoodEstablishmentsFoundException extends Exception {

  public NoFoodEstablishmentsFoundException() {
    super("No food establishments could be found.");
  }

  public NoFoodEstablishmentsFoundException(String message) {
    super(message);
  }

  public NoFoodEstablishmentsFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoFoodEstablishmentsFoundException(Throwable cause) {
    super(cause);
  }
}
