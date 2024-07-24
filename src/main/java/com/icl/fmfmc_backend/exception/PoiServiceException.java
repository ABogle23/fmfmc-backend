package com.icl.fmfmc_backend.exception;

public class PoiServiceException extends Throwable {

  public PoiServiceException() {}

  public PoiServiceException(String string) {}

  public PoiServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public PoiServiceException(Throwable cause) {
    super(cause);
  }
}
