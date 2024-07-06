package com.icl.fmfmc_backend.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
public class FoursquareRequest {

  //  private final String ll;
  //
  //  private final Integer radius;
  //  private final String categories;
  //  private final String fields =
  //      "fsq_id,name,categories,closed_bucket,distance,geocodes,location,price,rating,popularity";
  //
  //  private final Integer minPrice;
  //  private final Integer maxPrice;
  //  private final String openAt;
  //
  //  private final Boolean openNow;
  //
  //  private final String neLatLong;
  //
  //  private final String swLatLong;
  //
  //  private final Integer limit = 50;
  //
  //  private final String polygon;
  //
  //  private final String sortBy;

  private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

  public FoursquareRequest(
      String ll,
      Integer radius,
      String categories,
      String fields,
      Integer minPrice,
      Integer maxPrice,
      String openAt,
      Boolean openNow,
      String neLatLong,
      String swLatLong,
      Integer limit,
      String polygon,
      String sortBy) {
    if (ll != null) {
      this.queryParams.add("ll", ll);
    }
    if (radius != null) {
      this.queryParams.add("radius", radius.toString());
    }
    if (categories != null) {
      this.queryParams.add("categories", categories);
    }
    if (categories != null) {
      this.queryParams.add("fields", fields);
    }
    if (minPrice != null) {
      this.queryParams.add("minPrice", minPrice.toString());
    }
    if (maxPrice != null) {
      this.queryParams.add("maxPrice", maxPrice.toString());
    }
    if (openAt != null) {
      this.queryParams.add("openAt", openAt);
    }
    if (openNow != null) {
      this.queryParams.add("openNow", openNow.toString());
    }
    if (neLatLong != null) {
      this.queryParams.add("neLatLong", neLatLong);
    }
    if (swLatLong != null) {
      this.queryParams.add("swLatLong", swLatLong);
    }
    if (limit != null) {
      this.queryParams.add("limit", limit.toString());
    }
    if (polygon != null) {
      this.queryParams.add("polygon", polygon);
    }
    if (sortBy != null) {
      this.queryParams.add("sortBy", sortBy);
    }
  }
}
