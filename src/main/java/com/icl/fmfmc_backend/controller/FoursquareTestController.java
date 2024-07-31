package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.entity.FoodEstablishment.*;
import com.icl.fmfmc_backend.entity.enums.FoodCategory;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class FoursquareTestController {

  private final FoodEstablishmentBuilder requestBuilder = new FoursquareRequestBuilder();
  private final FoodEstablishmentService foodEstablishmentService;



  @GetMapping("/fs-basic-test-api-call")
  public String basicAPICall() {
    return "API call successful!";
  }

  @GetMapping("/fs-test-api-call")
  public List<FoodEstablishment> testApiCall() {



//    FoursquareRequest foursquareRequest =
//        new FoursquareRequestBuilder()
//            .setLl("51.472440,-0.042947")
//            .setRadius(10000)
//                .setCategories(List.of(FoodCategory.FOOD_RETAILER))
//                .setMinPrice(3)
//                .setMaxPrice(4)
//            .createFoursquareRequest();
//    FoursquareResponseDTO foursquareResponseDTO = foursquareClient.getFoodEstablishmentFromFoursquarePlacesApi(foursquareRequest);
////    return "API call successful!";
//    return foursquareResponseDTO;

        FoodEstablishmentRequest foodEstablishmentRequest =
                requestBuilder.setLatitude("51.472440")
                        .setLongitude("-0.042947")
//                        .setLongitude("-10.042947")
                        .setRadius(10000)
                        .setCategories(List.of(FoodCategory.FOOD_RETAILER))
                        .setMinPrice(3)
                        .setMaxPrice(4)
                        .build();

    List<FoodEstablishment> clusterFoodEstablishments =
            foodEstablishmentService.getFoodEstablishmentsByParam(foodEstablishmentRequest);

    if (clusterFoodEstablishments == null) {
      System.out.println("clusterFoodEstablishments is null");
    }

//    FoursquareResponseDTO foursquareResponseDTO = foursquareClient.getFoodEstablishmentFromFoursquarePlacesApi(foursquareRequest);
//    return "API call successful!";
    return clusterFoodEstablishments;

  }
}
