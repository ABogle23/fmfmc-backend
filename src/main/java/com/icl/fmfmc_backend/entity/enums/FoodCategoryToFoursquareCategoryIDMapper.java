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
    categoryToFoursquareIdMap.put(FoodCategory.CAFE, "13032"); // Example ID
    categoryToFoursquareIdMap.put(FoodCategory.BAR, "13003"); // Example ID
    categoryToFoursquareIdMap.put(FoodCategory.RESTAURANT, "13065"); // Example ID
    categoryToFoursquareIdMap.put(FoodCategory.FOOD_RETAILER, "17069,17099,17142"); // Example ID
  }

  public String mapCategoriesToFoursquareIds(List<FoodCategory> categories) {
    return categories.stream().map(categoryToFoursquareIdMap::get).collect(Collectors.joining(","));
  }
}
