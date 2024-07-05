package com.icl.fmfmc_backend.Routing;

import java.text.DecimalFormat;

public class CoordinateFormatter {
    private static final DecimalFormat df = new DecimalFormat("#.###");

    public static String formatCoordinate(double coordinate) {
        return df.format(coordinate);
    }
}