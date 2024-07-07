package com.icl.fmfmc_backend.service;


import com.icl.fmfmc_backend.entity.Charger;
import com.icl.fmfmc_backend.entity.Connection;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.entity.enums.ConnectionTypeToOcmMapper;
import com.icl.fmfmc_backend.repository.ChargerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Polygon;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerService {
    private final ChargerRepo chargerRepo;
    private final ConnectionTypeToOcmMapper connectionTypeToOcmMapper = new ConnectionTypeToOcmMapper();

    public List<Charger> getAllChargers(){
        return chargerRepo.findAll();
    }

    public Charger getChargerById(Long id){
        Optional<Charger> optionalCharger = chargerRepo.findById(id);
        if(optionalCharger.isPresent()){
            return optionalCharger.get();
        }
        log.info("Charger with id: {} doesn't exist", id);
        return null;
    }

    public List<Charger> getChargersWithinPolygon(Polygon polygon) {
        return chargerRepo.findAllWithinPolygon(polygon);
    }

    public List<Charger> getChargersByConnectionType(List<ConnectionType> type) {
        List<Integer> dbIds = connectionTypeToOcmMapper.mapConnectionTypeToDbIds(type);
        return chargerRepo.findByConnectionTypeIds(dbIds);
    }

    public List<Charger> getChargersByChargeSpeed(Integer minKwSpeed, Integer maxKwSpeed) {
        return chargerRepo.findByConnectionChargerSpeed(minKwSpeed, maxKwSpeed);
    }
    public List<Charger> getChargersByMinNoChargePoints(Integer minNoChargePoints) {
        return chargerRepo.findByMinNoChargePoints(minNoChargePoints);
    }


    @Transactional
    public Charger saveCharger (Charger charger){
            charger.setCreatedAt(LocalDateTime.now());
            charger.setUpdatedAt(LocalDateTime.now());

            Charger savedCharger = chargerRepo.save(charger);

            log.info("Charger with id: {} saved successfully", charger.getId());
            return savedCharger;
    }

    // TODO: implement updateCharger method
    public Charger updateCharger (Charger charger) {
        Optional<Charger> existingCharger = chargerRepo.findById(charger.getId());
        if (existingCharger.isPresent()) {
            charger.setCreatedAt(existingCharger.get().getCreatedAt()); // keep the original createdAt
            charger.setUpdatedAt(LocalDateTime.now()); // update the updatedAt to current time
            Charger updatedCharger = chargerRepo.save(charger);
            log.info("Charger with id: {} updated successfully", charger.getId());
            return updatedCharger;
        } else {
            log.info("Charger with id: {} doesn't exist", charger.getId());
            return null;
        }
    }



    public void deleteChargerById (Long id) {
        chargerRepo.deleteById(id);
    }

    public void deleteALLChargers () {
        chargerRepo.deleteAll();
    }



}
