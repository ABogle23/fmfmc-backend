package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.api.ElectricVehicleDto;
import com.icl.fmfmc_backend.entity.ElectricVehicle;
import com.icl.fmfmc_backend.repository.ElectricVehicleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElectricVehicleService {

  private final ElectricVehicleRepo electricVehicleRepo;

  public List<ElectricVehicle> findAllVehicles() {
    return electricVehicleRepo.findAll();
  }

  /**
   * Retrieves all electric vehicles in a compact form.
   *
   * @return a list of ElectricVehicleDto objects
   */
  public List<ElectricVehicleDto> findAllVehiclesCompact() {
    return electricVehicleRepo.findAllVehiclesCompact();
  }

  /**
   * Retrieves an electric vehicle by its ID.
   *
   * @param id the ID of the electric vehicle
   * @return the electric vehicle if found, otherwise null
   */
  public ElectricVehicle findElectricVehicleById(Long id) {
    Optional<ElectricVehicle> optionalElectricVehicle = electricVehicleRepo.findById(id);
    if (optionalElectricVehicle.isPresent()) {
      log.info("ElectricVehicle with id: {} found", id);
      return optionalElectricVehicle.get();
    }
    log.info("ElectricVehicle with id: {} doesn't exist", id);
    return null;
  }

  /**
   * Saves an electric vehicle entity.
   *
   * @param ev the electric vehicle entity to save
   * @return an Optional containing the saved electric vehicle entity if successful, otherwise an
   *     empty Optional
   */
  @Transactional
  public Optional<ElectricVehicle> saveElectricVehicle(ElectricVehicle ev) {
    log.info("Attempting to save ElectricVehicle: {}", ev.getModel());
    Optional<ElectricVehicle> existingEV = electricVehicleRepo.findByModel(ev.getModel());

    try {
      if (existingEV.isPresent()) {
        log.info(
            "Duplicate model found, updating existing ElectricVehicle with model: {}",
            ev.getModel());
        ElectricVehicle existingVehicle = existingEV.get();
        log.info("Existing ElectricVehicle: {}", existingVehicle.getModel());
        updateExistingVehicle(existingVehicle, ev);
        log.info("Updated ElectricVehicle: {}", existingVehicle.getModel());
        return Optional.of(existingVehicle);
      } else {
        log.info(
            "No existing ElectricVehicle found, creating new entry for model: {}", ev.getModel());
        ev.setCreatedAt(LocalDateTime.now());
        ev.setUpdatedAt(LocalDateTime.now());
        return Optional.of(electricVehicleRepo.save(ev));
      }
    } catch (Exception e) {
      log.error("Failed to save ElectricVehicle due to: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private void updateExistingVehicle(ElectricVehicle existing, ElectricVehicle updated) {
    log.info("Updating ElectricVehicle: {} with new values", existing.getModel());
    existing.setBrand(updated.getBrand());
    existing.setBatteryCapacity(updated.getBatteryCapacity());
    existing.setPricePerMile(updated.getPricePerMile());
    existing.setTopSpeed(updated.getTopSpeed());
    existing.setEvRange(updated.getEvRange());
    existing.setEfficiency(updated.getEfficiency());
    existing.setRapidChargeSpeed(updated.getRapidChargeSpeed());
    existing.setChargePortTypes(updated.getChargePortTypes());
    existing.setUpdatedAt(LocalDateTime.now());
  }
}
