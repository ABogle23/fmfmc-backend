package com.icl.fmfmc_backend.entity.enums;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class FoodCategoryToFoursquareCategoryIDMapper {
  private static final Map<FoodCategory, String> categoryToFoursquareIdMap =
      new EnumMap<>(FoodCategory.class);

  static {
    categoryToFoursquareIdMap.put(FoodCategory.CAFE, "13032");
    categoryToFoursquareIdMap.put(FoodCategory.BAR, "13003");
    categoryToFoursquareIdMap.put(FoodCategory.RESTAURANT, "13065");
    categoryToFoursquareIdMap.put(FoodCategory.FOOD_RETAILER, "17069,17099,17142");
    categoryToFoursquareIdMap.put(FoodCategory.SERVICE_STATION, "19007");
    categoryToFoursquareIdMap.put(FoodCategory.NEWSAGENT, "17099");
    categoryToFoursquareIdMap.put(FoodCategory.SUPERMARKET, "17142,17069");
    categoryToFoursquareIdMap.put(FoodCategory.PIZZA_RESTAURANT, "13064");
    categoryToFoursquareIdMap.put(FoodCategory.FAST_FOOD, "13145");
    categoryToFoursquareIdMap.put(FoodCategory.ASIAN_RESTAURANT, "13072");
    categoryToFoursquareIdMap.put(FoodCategory.ITALIAN_RESTAURANT, "13236");
    categoryToFoursquareIdMap.put(FoodCategory.GREEK_RESTAURANT, "13177");


  }

  public String mapCategoriesToFoursquareIds(List<FoodCategory> categories) {
    // add null check
    return categories.stream().map(categoryToFoursquareIdMap::get).collect(Collectors.joining(","));
  }
}
