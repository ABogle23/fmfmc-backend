package com.icl.fmfmc_backend.exception;

public class JourneyNotFoundException extends Exception {

    public JourneyNotFoundException() {
    super("No valid journey could be found.");
    }
  public JourneyNotFoundException(String message) {
    super(message);
  }


  public JourneyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

    public JourneyNotFoundException(Throwable cause) {
        super(cause);
    }



}
