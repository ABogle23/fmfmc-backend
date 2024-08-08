package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.dto.api.ElectricVehicleDto;
import com.icl.fmfmc_backend.dto.ElectricVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface ElectricVehicleRepo extends JpaRepository<ElectricVehicle, Long> {

    @Query("SELECT ev FROM ElectricVehicle ev WHERE ev.model = :model")
    Optional<ElectricVehicle> findByModel(String model);

    @Query("SELECT new com.icl.fmfmc_backend.dto.api.ElectricVehicleDto(ev.id, ev.brand, ev.model, ev.evRange, ev.batteryCapacity) FROM ElectricVehicle ev")
    List<ElectricVehicleDto> findAllVehiclesCompact();

}
