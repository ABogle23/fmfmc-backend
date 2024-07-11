package com.icl.fmfmc_backend.entity.enums;

public enum FoodCategory implements FmfmcEnum {
  // Primary Categories
  CAFE("Cafe", "cafe"),
  BAR("Bar","bar"),
  RESTAURANT("Restaurant","restaurant"),
  FOOD_RETAILER("Food Retailer","foodRetailer"),

  // Sub-categories under Restaurant
  ASIAN_RESTAURANT("Asian Restaurant","asianRestaurant"),
  ITALIAN_RESTAURANT("Italian Restaurant","italianRestaurant"),
  FAST_FOOD("Fast Food","fastFood"),
  SEAFOOD_RESTAURANT("Seafood Restaurant","seafoodRestaurant"),
  VEGAN_RESTAURANT("Vegan Restaurant","veganRestaurant"),
  GLUTEN_FREE_RESTAURANT("Gluten Free Restaurant","glutenFreeRestaurant"),
  BARBECUE("Barbecue","barbecue"),
  BURGER_JOINT("Burger Joint","burgerJoint"),
  PIZZA_RESTAURANT("Pizza Restaurant","pizzaRestaurant"),
  MEXICAN_RESTAURANT("Mexican Restaurant","mexicanRestaurant"),
  FINE_DINING("Fine Dining","fineDining"),
  CASUAL_DINING("Casual Dining","casualDining"),
  BUFFET("Buffet","buffet"),
  BISTRO("Bistro","bistro"),
  STEAKHOUSE("Steak house","steakhouse"),
  SUSHI_BAR("Sushi Bar","sushiBar"),
  RAMEN_SHOP("Ramen Shop","ramenShop"),
  TAPAS_BAR("Tapas Bar","tapasBar"),
  DINER("Diner","diner"),
  GREEK_RESTAURANT("Greek Restaurant","greekRestaurant"),

  // Sub-categories under Cafe
  INTERNET_CAFE("Internet Cafe","internetCafe"),
  COFFEE_SHOP("coffee Shop","coffeeShop"),
  TEA_HOUSE("Tea House","teaHouse"),

  // Sub-categories under Bar
  IRISH_PUB("Irish Pub","irishPub"),
  SPORTS_BAR("Sports Bar","sportsBar"),
  WINE_BAR("Wine Bar","wineBar"),
  BEER_GARDEN("Beer Garden","beerGarden"),
  COCKTAIL_LOUNGE("Cocktail Lounge","cocktailLounge"),

  // Sub-categories under Food Retailer
  SUPERMARKET("Supermarket","supermarket"),
  GROCERY("Grocery Store","groceryStore"),
  BAKERY("Bakery","bakery"),
  BUTCHER("Butcher","butcher"),
  FARMERS_MARKET("Farmers Market","farmersMarket"),
  SERVICE_STATION("Service Station","serviceStation"),
  NEWSAGENT("Newsagent","newsagent");

  private final String displayName;
  private final String apiName;

  FoodCategory(String displayName, String apiName) {
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

//  public static FoodCategory getFoodCategoryFromDisplayName(String apiName) {
//    for (FoodCategory category : FoodCategory.values()) {
//      if (category.getApiName().equalsIgnoreCase(apiName)) {
//        return category;
//      }
//    }
//    throw new IllegalArgumentException("No constant with displayName " + apiName + " found");
//  }
}
