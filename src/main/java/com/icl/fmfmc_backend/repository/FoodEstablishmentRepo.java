package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.Connection;
import com.icl.fmfmc_backend.entity.FoodEstablishment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodEstablishmentRepo extends JpaRepository<FoodEstablishment, String> {
}