package com.icl.fmfmc_backend.exception.service;

public class NoFoodEstablishmentsInRangeOfChargerException extends Exception {


    public NoFoodEstablishmentsInRangeOfChargerException() {
        super("No restaurants within range of charger could be found.");
    }

    public NoFoodEstablishmentsInRangeOfChargerException(String message) {
        super(message);
    }

    public NoFoodEstablishmentsInRangeOfChargerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFoodEstablishmentsInRangeOfChargerException(Throwable cause) {
        super(cause);
    }
}
