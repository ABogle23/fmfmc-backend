package com.icl.fmfmc_backend.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
public class FoursquareRequest {

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
      this.queryParams.add("min_price", minPrice.toString());
    }
    if (maxPrice != null) {
      this.queryParams.add("max_price", maxPrice.toString());
    }
    if (openAt != null) {
      this.queryParams.add("open_at", openAt);
    }
    if (openNow != null) {
      this.queryParams.add("open_now", openNow.toString());
    }
    if (neLatLong != null) {
      this.queryParams.add("ne", neLatLong);
    }
    if (swLatLong != null) {
      this.queryParams.add("sw", swLatLong);
    }
    if (limit != null) {
      this.queryParams.add("limit", limit.toString());
    }
    if (polygon != null) {
      this.queryParams.add("polygon", polygon);
    }
    if (sortBy != null) {
      this.queryParams.add("sort", sortBy);
    }
  }
}
