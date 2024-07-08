package com.icl.fmfmc_backend.entity.enums;

public enum AccessType implements FmfmcEnum {
    PUBLIC("Public", "public"),
    RESTRICTED("Restricted", "restricted"),
    PRIVATE("Private", "private");

    private final String displayName;
    private final String apiName;

    AccessType(String displayName, String apiName) {
        this.displayName = displayName;
        this.apiName = apiName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }

}
