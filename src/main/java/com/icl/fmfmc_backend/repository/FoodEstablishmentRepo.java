package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.foodEstablishment.FoodEstablishment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodEstablishmentRepo extends JpaRepository<FoodEstablishment, String> {
}