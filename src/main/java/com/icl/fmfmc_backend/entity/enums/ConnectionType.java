package com.icl.fmfmc_backend.entity.enums;

public enum ConnectionType implements FmfmcEnum {
  TYPE2("Type 2", "type2"),
  CHADEMO("CHAdeMO", "chademo"),
  CCS("CCS", "ccs"),
  TESLA("Tesla", "tesla"),
  DOMESTIC_3_PIN("Domestic 3-pin", "domestic3Pin"),
  TYPE1("Type 1", "type1");

  private final String displayName;
  private final String apiName;

  ConnectionType(String displayName, String apiName) {
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

  //    public static ConnectionType getFoodCategoryFromDisplayName(String apiName) {
  //        for (ConnectionType connector : ConnectionType.values()) {
  //            if (connector.getApiName().equalsIgnoreCase(apiName)) {
  //                return connector;
  //            }
  //        }
  //        throw new IllegalArgumentException("No constant with displayName " + apiName + "
  // found");
  //    }

}
