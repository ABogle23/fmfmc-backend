package com.icl.fmfmc_backend.entity.enums;

public enum FoodCategory implements FmfmcEnum {
  // Primary Categories
  CAFE("Cafe", "cafe", null),
  BAR("Bar","bar",null),
  RESTAURANT("Restaurant","restaurant",null),
  FOOD_RETAILER("Food Retailer","foodRetailer",null),

  // Sub-categories under Restaurant
  ASIAN_RESTAURANT("Asian Restaurant","asianRestaurant", RESTAURANT),
  ITALIAN_RESTAURANT("Italian Restaurant","italianRestaurant", RESTAURANT),
  FAST_FOOD("Fast Food","fastFood", RESTAURANT),
  SEAFOOD_RESTAURANT("Seafood Restaurant","seafoodRestaurant", RESTAURANT),
  VEGAN_RESTAURANT("Vegan Restaurant","veganRestaurant", RESTAURANT),
  GLUTEN_FREE_RESTAURANT("Gluten Free Restaurant","glutenFreeRestaurant", RESTAURANT),
  BARBECUE("Barbecue","barbecue", RESTAURANT),
  BURGER_JOINT("Burger Joint","burgerJoint", RESTAURANT),
  PIZZA_RESTAURANT("Pizza Restaurant","pizzaRestaurant", RESTAURANT),
  MEXICAN_RESTAURANT("Mexican Restaurant","mexicanRestaurant", RESTAURANT),
  FINE_DINING("Fine Dining","fineDining", RESTAURANT),
  CASUAL_DINING("Casual Dining","casualDining", RESTAURANT),
  BUFFET("Buffet","buffet", RESTAURANT),
  BISTRO("Bistro","bistro", RESTAURANT),
  STEAKHOUSE("Steak house","steakhouse", RESTAURANT),
  SUSHI_BAR("Sushi Bar","sushiBar", RESTAURANT),
  RAMEN_SHOP("Ramen Shop","ramenShop", RESTAURANT),
  TAPAS_BAR("Tapas Bar","tapasBar", RESTAURANT),
  DINER("Diner","diner", RESTAURANT),
  GREEK_RESTAURANT("Greek Restaurant","greekRestaurant", RESTAURANT),

  // Sub-categories under Cafe
  INTERNET_CAFE("Internet Cafe","internetCafe", BAR),
  COFFEE_SHOP("coffee Shop","coffeeShop", BAR),
  TEA_HOUSE("Tea House","teaHouse", BAR),

  // Sub-categories under Bar
  IRISH_PUB("Irish Pub","irishPub", BAR),
  SPORTS_BAR("Sports Bar","sportsBar", BAR),
  WINE_BAR("Wine Bar","wineBar", BAR),
  BEER_GARDEN("Beer Garden","beerGarden", BAR),
  COCKTAIL_LOUNGE("Cocktail Lounge","cocktailLounge", BAR),

  // Sub-categories under Food Retailer
  SUPERMARKET("Supermarket","supermarket", FOOD_RETAILER),
  GROCERY("Grocery Store","groceryStore", FOOD_RETAILER),
  BAKERY("Bakery","bakery", FOOD_RETAILER),
  BUTCHER("Butcher","butcher", FOOD_RETAILER),
  FARMERS_MARKET("Farmers Market","farmersMarket", FOOD_RETAILER),
  SERVICE_STATION("Service Station","serviceStation", FOOD_RETAILER),
  NEWSAGENT("Newsagent","newsagent", FOOD_RETAILER),;

  private final String displayName;
  private final String apiName;

  private final FoodCategory parent;

  FoodCategory(String displayName, String apiName, FoodCategory parent) {
    this.displayName = displayName;
    this.apiName = apiName;
    this.parent = parent;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getApiName() {
    return apiName;
  }

  public FoodCategory getParent() {
        return parent;
  }

//  public static FoodCategory getFoodCategoryFromDisplayName(String apiName) {
//    for (FoodCategory category : FoodCategory.values()) {
//      if (category.getApiName().equalsIgnoreCase(apiName)) {
//        return category;
//      }
//    }
//    throw new IllegalArgumentException("No constant with displayName " + apiName + " found");
//  }
}
