package com.icl.fmfmc_backend.exception;

public class NoFoodEstablishmentsInRangeofChargerException extends Exception {


    public NoFoodEstablishmentsInRangeofChargerException() {
        super("No restaurants within range of charger could be found.");
    }

    public NoFoodEstablishmentsInRangeofChargerException(String message) {
        super(message);
    }

    public NoFoodEstablishmentsInRangeofChargerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFoodEstablishmentsInRangeofChargerException(Throwable cause) {
        super(cause);
    }
}
