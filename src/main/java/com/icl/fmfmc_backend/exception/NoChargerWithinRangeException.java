package com.icl.fmfmc_backend.exception;

public class NoChargerWithinRangeException extends Exception {

    public NoChargerWithinRangeException() {
        super("No charger within range could be found.");
    }

    public NoChargerWithinRangeException(String message) {
        super(message);
    }

    public NoChargerWithinRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoChargerWithinRangeException(Throwable cause) {
        super(cause);
    }

}
