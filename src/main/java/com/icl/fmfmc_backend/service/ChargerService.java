package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.Charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Charger.Connection;
import com.icl.fmfmc_backend.entity.Routing.Route;
import com.icl.fmfmc_backend.entity.enums.AccessTypeToOcmMapper;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.entity.enums.ConnectionTypeToOcmMapper;
import com.icl.fmfmc_backend.repository.ChargerRepo;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Polygon;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerService {
  private final ChargerRepo chargerRepo;
  private final ConnectionTypeToOcmMapper connectionTypeToOcmMapper =
      new ConnectionTypeToOcmMapper();
  private final AccessTypeToOcmMapper accessTypeToOcmMapper = new AccessTypeToOcmMapper();

  public List<Charger> getAllChargers() {
    return chargerRepo.findAll();
  }

  public Charger getChargerById(Long id) {
    Optional<Charger> optionalCharger = chargerRepo.findById(id);
    if (optionalCharger.isPresent()) {
      log.info("Charger with id: {} found", id);
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

  public List<Charger> getChargersWithinRadius(Point minNoChargePoints, Double radius) {
    return chargerRepo.findChargersWithinRadius(minNoChargePoints, radius);
  }

  public List<Charger> getChargersByParams(ChargerQuery query) {

    List<Integer> mappedConnectionTypeIds =
        connectionTypeToOcmMapper.mapConnectionTypeToDbIds(query.getConnectionTypeIds());
    String connectionTypeIds =
        String.join(
            ",",
            mappedConnectionTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    List<Integer> mappedAccessTypeIds =
        accessTypeToOcmMapper.mapAccessTypeToDbIds(query.getAccessTypeIds());
    String accessTypeIds =
        String.join(
            ",", mappedAccessTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    return chargerRepo.findChargersByParams(
        query.getPolygon(),
        query.getPoint(),
        query.getRadius(),
        connectionTypeIds,
        accessTypeIds,
        query.getMinKwChargeSpeed(),
        query.getMaxKwChargeSpeed(),
        query.getMinNoChargePoints());
  }

  public List<Point> getChargerLocationsByParams(ChargerQuery query) {

    List<Integer> mappedConnectionTypeIds =
            connectionTypeToOcmMapper.mapConnectionTypeToDbIds(query.getConnectionTypeIds());
    String connectionTypeIds =
            String.join(
                    ",",
                    mappedConnectionTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    List<Integer> mappedAccessTypeIds =
            accessTypeToOcmMapper.mapAccessTypeToDbIds(query.getAccessTypeIds());
    String accessTypeIds =
            String.join(
                    ",", mappedAccessTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    return chargerRepo.findChargerLocationsByParams(
            query.getPolygon(),
            query.getPoint(),
            query.getRadius(),
            connectionTypeIds,
            accessTypeIds,
            query.getMinKwChargeSpeed(),
            query.getMaxKwChargeSpeed(),
            query.getMinNoChargePoints());
  }

  public Double getHighestPowerConnectionByTypeInCharger(Charger charger, Route route) {

    List<Integer> mappedConnectionTypeIds =
            connectionTypeToOcmMapper.mapConnectionTypeToDbIds(route.getConnectionTypes());
    String connectionTypeIds =
            String.join(
                    ",",
                    mappedConnectionTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    Double chargeSpeed = chargerRepo.findHighestPowerConnectionByTypeInCharger(
            charger.getId(), connectionTypeIds
            );
    System.out.println("Highest power connection speed: " + chargeSpeed);
    return chargeSpeed;
  }

  @PersistenceContext private EntityManager entityManager;

  public List<Charger> findChargersByParam(
      Polygon polygon,
      Point point,
      Double radius,
      List<ConnectionType> connectionTypes,
      Integer minKwChargeSpeed,
      Integer maxKwChargeSpeed,
      Integer minNoChargePoints) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Charger> cq = cb.createQuery(Charger.class);
    Root<Charger> charger = cq.from(Charger.class);
    Join<Charger, Connection> connection = charger.join("connections", JoinType.INNER);

    List<Predicate> predicates = new ArrayList<>();

    // spatial params
    if (polygon != null) {
      predicates.add(
          cb.isTrue(
              cb.function(
                  "ST_Within", Boolean.class, charger.get("location"), cb.literal(polygon))));
    }
    if (point != null && radius != null) {
      predicates.add(
          cb.lessThanOrEqualTo(
              cb.function(
                  "ST_Distance_Sphere", Double.class, charger.get("location"), cb.literal(point)),
              radius));
    }

    // connection type IDs
    if (connectionTypes != null && !connectionTypes.isEmpty()) {
      List<Integer> connectionTypeIds =
          connectionTypeToOcmMapper.mapConnectionTypeToDbIds(connectionTypes);
      predicates.add(connection.get("connectionTypeID").in(connectionTypeIds));
    }

    // min charge speed
    if (minKwChargeSpeed != null) {
      predicates.add(cb.greaterThanOrEqualTo(connection.get("powerKW"), minKwChargeSpeed));
    } else {
      predicates.add(cb.greaterThanOrEqualTo(connection.get("powerKW"), cb.literal(0)));
    }

    // max charge speed
    if (maxKwChargeSpeed != null) {
      predicates.add(cb.lessThanOrEqualTo(connection.get("powerKW"), maxKwChargeSpeed));
    }

    // min number of charge points
    predicates.add(
        cb.greaterThanOrEqualTo(
            charger.get("numberOfPoints"),
            cb.coalesce(cb.literal(minNoChargePoints), cb.literal(1))));

    cq.where(cb.and(predicates.toArray(new Predicate[0])));
    return entityManager.createQuery(cq).getResultList();
  }

  @Transactional
  public Charger saveCharger(Charger charger) {
    charger.setCreatedAt(LocalDateTime.now());
    charger.setUpdatedAt(LocalDateTime.now());

    Charger savedCharger = chargerRepo.save(charger);

    log.info("Charger with id: {} saved successfully", charger.getId());
    return savedCharger;
  }

  // TODO: implement updateCharger method
  public Charger updateCharger(Charger charger) {
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

  public void deleteChargerById(Long id) {
    chargerRepo.deleteById(id);
  }

  public void deleteALLChargers() {
    chargerRepo.deleteAll();
  }
}
