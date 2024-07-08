package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.Integration.FoursquareClient;
import com.icl.fmfmc_backend.dto.FoodEstablishment.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.*;
import com.icl.fmfmc_backend.entity.FoodEstablishment.Category;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoodEstablishment;
import com.icl.fmfmc_backend.entity.FoodEstablishment.FoursquareRequest;
import com.icl.fmfmc_backend.repository.FoodEstablishmentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodEstablishmentService {
    private final FoodEstablishmentRepo foodEstablishmentRepo;
    private final FoursquareClient foursquareClient;

    public List<FoodEstablishment> getAllFoodEstablishments(){
        return foodEstablishmentRepo.findAll();
    }

    public FoodEstablishment getFoodEstablishmentById(String id){
        Optional<FoodEstablishment> optionalFoodEstablishment = foodEstablishmentRepo.findById(id);
        if(optionalFoodEstablishment.isPresent()){
            return optionalFoodEstablishment.get();
        }
        log.info("FoodEstablishment with id: {} doesn't exist", id);
        return null;
    }

    @Transactional
    public FoodEstablishment saveFoodEstablishment (FoodEstablishment foodEstablishment){
        foodEstablishment.setCreatedAt(LocalDateTime.now());
        FoodEstablishment savedFoodEstablishment = foodEstablishmentRepo.save(foodEstablishment);
        log.info("FoodEstablishment with id: {} saved successfully", foodEstablishment.getId());
        return savedFoodEstablishment;
    }

    public FoodEstablishment updateFoodEstablishment (FoodEstablishment foodEstablishment) {
        Optional<FoodEstablishment> existingFoodEstablishment = foodEstablishmentRepo.findById(foodEstablishment.getId());
        if (existingFoodEstablishment.isPresent()) {
            FoodEstablishment updatedFoodEstablishment = foodEstablishmentRepo.save(foodEstablishment);
            log.info("FoodEstablishment with id: {} updated successfully", foodEstablishment.getId());
            return updatedFoodEstablishment;
        } else {
            log.info("FoodEstablishment with id: {} doesn't exist", foodEstablishment.getId());
            return null;
        }
    }

    public void deleteFoodEstablishmentById (String id) {
        foodEstablishmentRepo.deleteById(id);
    }

    public void deleteAllFoodEstablishemts () {
        foodEstablishmentRepo.deleteAll();
    }


//    public List<FoodEstablishment> getFoodEstablishmentsFromFoursquare(MultiValueMap<String, String> parameters) {
//        FoursquareResponseDTO response = foursquareClient.getFoodEstablishmentFromFoursquarePlacesApi(parameters);
//        if (response != null && response.getResults() != null) {
//            response.getResults().forEach(this::saveFoodEstablishmentFromDTO);
//        }
//        return foodEstablishmentRepo.findAll();
//    }

    public List<FoodEstablishment> getFoodEstablishmentsByParam(FoursquareRequest foursquareRequest) {
        List<FoodEstablishment> savedEstablishments = new ArrayList<>();
        FoursquareResponseDTO response = foursquareClient.getFoodEstablishmentFromFoursquarePlacesApi(foursquareRequest);

        if (response != null && response.getResults() != null) {
            response.getResults().forEach(placeDTO -> {
                FoodEstablishment establishment = saveFoodEstablishmentFromDTO(placeDTO);
                savedEstablishments.add(establishment);
            });
        }
        log.info("Fetched {} food establishments from Foursquare", savedEstablishments.size());
        return savedEstablishments;
    }


    private FoodEstablishment saveFoodEstablishmentFromDTO(FoursquareResponseDTO.FoursquarePlaceDTO placeDTO) {
        FoodEstablishment establishment = convertToFoodEstablishment(placeDTO);
        foodEstablishmentRepo.save(establishment);
        return establishment;
    }

    private FoodEstablishment convertToFoodEstablishment(
            FoursquareResponseDTO.FoursquarePlaceDTO dto) {
        FoodEstablishment establishment = new FoodEstablishment();
        establishment.setId(dto.getFsqId());
        establishment.setName(dto.getName());
        establishment.setAddress(
                new AddressInfo(
                        dto.getFsqId(),
                        dto.getLocation().getFormattedAddress(),
                        dto.getLocation().getLocality(),
                        dto.getLocation().getPostcode()));
        establishment.setCategories(
                dto.getCategories().stream()
                        .map(cat -> new Category(cat.getId(), cat.getName()))
                        .collect(Collectors.toList()));
        establishment.setGeocodes(
                new GeoCoordinates(
                        dto.getGeocodes().getMain().getLongitude(), dto.getGeocodes().getMain().getLatitude()));
        establishment.setClosedStatus(dto.getClosedBucket());
        establishment.setPopularity(dto.getPopularity());
        establishment.setPrice(dto.getPrice());
        establishment.setRating(dto.getRating());
        establishment.setCreatedAt(LocalDateTime.now());
        establishment.setLocation(
                new GeometryFactory()
                        .createPoint(
                                new Coordinate(
                                        dto.getGeocodes().getMain().getLongitude(),
                                        dto.getGeocodes().getMain().getLatitude())));
        return establishment;
    }


}