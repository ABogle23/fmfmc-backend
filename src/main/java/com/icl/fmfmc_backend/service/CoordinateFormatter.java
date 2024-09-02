package com.icl.fmfmc_backend.service;

import java.text.DecimalFormat;

// only used in foursqure polyline converter which is also not used
@Deprecated
public class CoordinateFormatter {
    private static final DecimalFormat df = new DecimalFormat("#.###");

    public static String formatCoordinate(double coordinate) {
        return df.format(coordinate);
    }
}