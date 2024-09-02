package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.dto.api.ElectricVehicleDto;
import com.icl.fmfmc_backend.entity.ElectricVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

/** Repository interface for ElectricVehicle entities. */
public interface ElectricVehicleRepo extends JpaRepository<ElectricVehicle, Long> {
  /**
   * Finds an ElectricVehicle by its model.
   *
   * @param model the model of the electric vehicle
   * @return an Optional containing the found ElectricVehicle, or empty if not found
   */
  @Query("SELECT ev FROM ElectricVehicle ev WHERE ev.model = :model")
  Optional<ElectricVehicle> findByModel(String model);

  /**
   * Retrieves a list of ElectricVehicleDto objects with compact information.
   *
   * @return a list of ElectricVehicleDto objects
   */
  @Query(
      "SELECT new com.icl.fmfmc_backend.dto.api.ElectricVehicleDto(ev.id, ev.brand, ev.model, ev.evRange, ev.batteryCapacity) FROM ElectricVehicle ev")
  List<ElectricVehicleDto> findAllVehiclesCompact();
}
