package com.icl.fmfmc_backend.exception;

public class NoRouteFoundException extends Throwable {
  public NoRouteFoundException(String string) {
    super(string);
  }

  public NoRouteFoundException() {
    super("No route could be found.");
  }

  public NoRouteFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoRouteFoundException(Throwable cause) {
    super(cause);
  }
}
