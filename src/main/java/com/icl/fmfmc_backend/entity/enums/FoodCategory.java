package com.icl.fmfmc_backend.entity.enums;

public enum FoodCategory {
  // Primary Categories
  CAFE("cafe"),
  BAR("bar"),
  RESTAURANT("restaurant"),
  FOOD_RETAILER("foodRetailer"),

  // Sub-categories under Restaurant
  ASIAN_RESTAURANT("asianRestaurant"),
  ITALIAN_RESTAURANT("italianRestaurant"),
  FAST_FOOD("fastFood"),
  SEAFOOD_RESTAURANT("seafoodRestaurant"),
  VEGAN_RESTAURANT("veganRestaurant"),
  GLUTEN_FREE_RESTAURANT("glutenFreeRestaurant"),
  BARBECUE("barbecue"),
  BURGER_JOINT("burgerJoint"),
  PIZZA_SHOP("pizzaShop"),
  MEXICAN_RESTAURANT("mexicanRestaurant"),
  FINE_DINING("fineDining"),
  CASUAL_DINING("casualDining"),
  BUFFET("buffet"),
  BISTRO("bistro"),
  STEAKHOUSE("steakhouse"),
  SUSHI_BAR("sushiBar"),
  RAMEN_SHOP("ramenShop"),
  TAPAS_BAR("tapasBar"),
  DINER("diner"),

  // Sub-categories under Cafe
  INTERNET_CAFE("internetCafe"),
  COFFEE_SHOP("coffeeShop"),
  TEA_HOUSE("teaHouse"),

  // Sub-categories under Bar
  IRISH_PUB("irishPub"),
  SPORTS_BAR("sportsBar"),
  WINE_BAR("wineBar"),
  BEER_GARDEN("beerGarden"),
  COCKTAIL_LOUNGE("cocktailLounge"),

  // Sub-categories under Food Retailer
  SUPERMARKET("supermarket"),
  GROCERY("groceryStore"),
  BAKERY("bakery"),
  BUTCHER("butcher"),
  FARMERS_MARKET("farmersMarket"),
  SERVICE_STATION("ServiceStation"),
  NEWSAGENT("newsagent");

  private final String displayName;

  FoodCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static FoodCategory getFoodCategoryFromDisplayName(String displayName) {
    for (FoodCategory category : FoodCategory.values()) {
      if (category.getDisplayName().equalsIgnoreCase(displayName)) {
        return category;
      }
    }
    throw new IllegalArgumentException("No constant with displayName " + displayName + " found");
  }

}
