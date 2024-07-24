package com.icl.fmfmc_backend.entity.enums;

public enum FallbackStrategy {
  RELAXED_CHARGING_CONSTRAINTS("Relaxed charging constraints"),

  EXPANDED_CHARGER_SEARCH_AREA("Expanded charger search area"),

  EXPANDED_CHARGER_SPEED_RANGE("Expanded charger speed min/max range"),

  EXPANDED_EATING_OPTION_SEARCH_AREA("Expanded eating option search area"),
  EXPANDED_EATING_OPTION_STOPPING_RANGE("Expanded eating option stopping range"),

  EXPANDED_EATING_OPTION_CATEGORY_SEARCH("Expanded eating option category search"),

  EXPANDED_EATING_OPTION_PRICE_RANGE("Expanded eating option price range"),

  SKIPPED_EATING_OPTION("Eating option failed, route returned without eating option"),;
  private String description;

  FallbackStrategy(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
