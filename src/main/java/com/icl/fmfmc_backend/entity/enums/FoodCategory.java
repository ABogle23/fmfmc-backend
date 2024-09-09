package com.icl.fmfmc_backend.entity.enums;

public enum FoodCategory implements FmfmcEnum {
  // Primary Categories
  CAFE("Cafe", "cafe", null),
  BAR("Bar","bar",null),
  RESTAURANT("Restaurant","restaurant",null),
  FOOD_RETAILER("Food Retailer","food_retailer",null),

  // Sub-categories under Restaurant
  ASIAN_RESTAURANT("Asian Restaurant","asian_restaurant", RESTAURANT),
  GASTROPUB("Gastropub","gastropub", RESTAURANT),
  ITALIAN_RESTAURANT("Italian Restaurant","italian_restaurant", RESTAURANT),
  FAST_FOOD("Fast Food","fastFood", RESTAURANT),
  SEAFOOD_RESTAURANT("Seafood Restaurant","seafood_restaurant", RESTAURANT),
  VEGAN_RESTAURANT("Vegan Restaurant","vegan_restaurant", RESTAURANT),
  GLUTEN_FREE_RESTAURANT("Gluten Free Restaurant","gluten_free_restaurant", RESTAURANT),
  BARBECUE("Barbecue","barbecue", RESTAURANT),
  BURGER_JOINT("Burger Joint","burger_joint", RESTAURANT),
  MEXICAN_RESTAURANT("Mexican Restaurant","mexican_restaurant", RESTAURANT),
  FINE_DINING("Fine Dining","fine_dining", RESTAURANT),
  CASUAL_DINING("Casual Dining","casual_dining", RESTAURANT),
  BUFFET("Buffet","buffet", RESTAURANT),
  BISTRO("Bistro","bistro", RESTAURANT),
  STEAKHOUSE("Steak house","steakhouse", RESTAURANT),
  TAPAS_BAR("Tapas Bar","tapas_bar", RESTAURANT),
  DINER("Diner","diner", RESTAURANT),
  GREEK_RESTAURANT("Greek Restaurant","greek_restaurant", RESTAURANT),
  SUSHI_BAR("Sushi Bar","sushi_bar", ASIAN_RESTAURANT),
  RAMEN_SHOP("Ramen Shop","ramen_shop", ASIAN_RESTAURANT),
  PIZZA_RESTAURANT("Pizza Restaurant","pizza_restaurant", ITALIAN_RESTAURANT),
  INDIAN_RESTAURANT("Indian Restaurant","indian_restaurant", ASIAN_RESTAURANT),
  GUJARATI_RESTAURANT("Gujarati Restaurant","gujarati_restaurant", INDIAN_RESTAURANT),

  // Sub-categories under Cafe
  INTERNET_CAFE("Internet Cafe","internet_cafe", CAFE),
  COFFEE_SHOP("coffee Shop","coffee_shop", CAFE),
  TEA_HOUSE("Tea House","tea_house", CAFE),

  // Sub-categories under Bar
  IRISH_PUB("Irish Pub","irish_pub", BAR),
  SPORTS_BAR("Sports Bar","sports_par", BAR),
  WINE_BAR("Wine Bar","wine_bar", BAR),
  BEER_GARDEN("Beer Garden","beer_garden", BAR),
  COCKTAIL_LOUNGE("Cocktail Lounge","cocktail_lounge", BAR),

  // Sub-categories under Food Retailer
  SUPERMARKET("Supermarket","supermarket", FOOD_RETAILER),
  GROCERY("Grocery Store","grocery_store", FOOD_RETAILER),
  BAKERY("Bakery","bakery", FOOD_RETAILER),
  BUTCHER("Butcher","butcher", FOOD_RETAILER),
  FARMERS_MARKET("Farmers Market","farmers_market", FOOD_RETAILER),
  SERVICE_STATION("Service Station","service_station", FOOD_RETAILER),
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
