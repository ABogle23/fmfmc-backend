package com.icl.fmfmc_backend.exception;

public class DirectionsClientException extends Exception {
  public DirectionsClientException() {
    super("Error occurred while fetching directions from active client." );
  }

  public DirectionsClientException(String message) {
    super(message);
  }

  public DirectionsClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public DirectionsClientException(Throwable cause) {
    super(cause);
  }
}
