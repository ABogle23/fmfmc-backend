package com.icl.fmfmc_backend.entity.FoodEstablishment;

import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import com.icl.fmfmc_backend.entity.enums.FoodCategoryToFoursquareCategoryIDMapper;

import java.util.List;

public class FoursquareRequestBuilder implements FoodEstablishmentBuilder {

  private final FoodCategoryToFoursquareCategoryIDMapper categoryMapper =
      new FoodCategoryToFoursquareCategoryIDMapper();
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
  private String latitude;
  private String longitude;

  public FoursquareRequestBuilder setLl(String ll) {
    this.ll = ll;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setLatitude(String latitude) {
    this.latitude = latitude;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setLongitude(String longitude) {
    this.longitude = longitude;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setRadius(Integer radius) {
    this.radius = radius;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setCategories(List<FoodCategory> categories) {
    this.categories = categoryMapper.mapCategoriesToFoursquareIds(categories);
    return this;
  }

  @Override
  public FoursquareRequestBuilder setMinPrice(Integer minPrice) {
    this.minPrice = minPrice;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setMaxPrice(Integer maxPrice) {
    this.maxPrice = maxPrice;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setOpenAt(String openAt) {
    this.openAt = openAt;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setOpenNow(Boolean openNow) {
    this.openNow = openNow;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setNeLatLong(String neLatLong) {
    this.neLatLong = neLatLong;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setSwLatLong(String swLatLong) {
    this.swLatLong = swLatLong;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setPolygon(String polygon) {
    this.polygon = polygon;
    return this;
  }

  @Override
  public FoursquareRequestBuilder setSortBy(String sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  @Override
  public FoursquareRequest build() {

    if (latitude != null && longitude != null) {
      ll = latitude + "," + longitude;
    }

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
