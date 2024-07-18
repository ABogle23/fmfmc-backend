package com.icl.fmfmc_backend.entity.FoodEstablishment;

import com.icl.fmfmc_backend.entity.enums.FoodCategory;

import java.util.List;

public interface FoodEstablishmentBuilder {

  FoodEstablishmentBuilder setLatitude(String latitude);

  FoodEstablishmentBuilder setLongitude(String longitude);

  FoodEstablishmentBuilder setRadius(Integer radius);

  FoodEstablishmentBuilder setCategories(List<FoodCategory> categories);

  FoodEstablishmentBuilder setMinPrice(Integer minPrice);

  FoodEstablishmentBuilder setMaxPrice(Integer maxPrice);

  FoodEstablishmentBuilder setOpenAt(String openAt);

  FoodEstablishmentBuilder setOpenNow(Boolean openNow);

  FoodEstablishmentBuilder setNeLatLong(String neLatLong);

  FoodEstablishmentBuilder setSwLatLong(String swLatLong);

  FoodEstablishmentBuilder setPolygon(String polygon);

  FoodEstablishmentBuilder setSortBy(String sortBy);

  FoodEstablishmentRequest build();
}
