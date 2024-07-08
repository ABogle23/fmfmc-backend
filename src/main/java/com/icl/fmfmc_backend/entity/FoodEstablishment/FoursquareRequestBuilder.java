package com.icl.fmfmc_backend.entity.FoodEstablishment;

import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import com.icl.fmfmc_backend.entity.enums.FoodCategoryToFoursquareCategoryIDMapper;

import java.util.List;

public class FoursquareRequestBuilder {

  private final FoodCategoryToFoursquareCategoryIDMapper categoryMapper = new FoodCategoryToFoursquareCategoryIDMapper();
  private String ll;
  private Integer radius;
  private String categories = "13065";
  private final String fields =
      "fsq_id,name,categories,closed_bucket,distance,geocodes,location,price,rating,popularity";
  private Integer minPrice;
  private Integer maxPrice;
  private String openAt;
  private Boolean openNow;
  private String neLatLong;
  private String swLatLong;

  private final Integer limit = 50;
  private String polygon;
  private String sortBy;

  public FoursquareRequestBuilder setLl(String ll) {
    this.ll = ll;
    return this;
  }

  public FoursquareRequestBuilder setRadius(Integer radius) {
    this.radius = radius;
    return this;
  }

  public FoursquareRequestBuilder setCategories(List<FoodCategory> categories) {
    this.categories = categoryMapper.mapCategoriesToFoursquareIds(categories);
    return this;
  }

  public FoursquareRequestBuilder setMinPrice(Integer minPrice) {
    this.minPrice = minPrice;
    return this;
  }

  public FoursquareRequestBuilder setMaxPrice(Integer maxPrice) {
    this.maxPrice = maxPrice;
    return this;
  }

  public FoursquareRequestBuilder setOpenAt(String openAt) {
    this.openAt = openAt;
    return this;
  }

  public FoursquareRequestBuilder setOpenNow(Boolean openNow) {
    this.openNow = openNow;
    return this;
  }

  public FoursquareRequestBuilder setNeLatLong(String neLatLong) {
    this.neLatLong = neLatLong;
    return this;
  }

  public FoursquareRequestBuilder setSwLatLong(String swLatLong) {
    this.swLatLong = swLatLong;
    return this;
  }

  public FoursquareRequestBuilder setPolygon(String polygon) {
    this.polygon = polygon;
    return this;
  }

  public FoursquareRequestBuilder setSortBy(String sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  public FoursquareRequest createFoursquareRequest() {
    return new FoursquareRequest(
        ll,
        radius,
        categories,
        fields,
        minPrice,
        maxPrice,
        openAt,
        openNow,
        neLatLong,
        swLatLong,
        limit,
        polygon,
        sortBy);
  }
}
