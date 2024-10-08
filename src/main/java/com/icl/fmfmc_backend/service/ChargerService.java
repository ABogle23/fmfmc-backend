package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.dto.charger.ChargerQuery;
import com.icl.fmfmc_backend.entity.charger.Charger;
import com.icl.fmfmc_backend.entity.routing.Journey;
import com.icl.fmfmc_backend.entity.enums.AccessTypeToOcmMapper;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import com.icl.fmfmc_backend.entity.enums.ConnectionTypeToOcmMapper;
import com.icl.fmfmc_backend.repository.ChargerRepo;
import com.icl.fmfmc_backend.util.LogExecutionTime;
import com.icl.fmfmc_backend.util.LogMessages;

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

/** Service class for managing Charger entities. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerService {
  private final ChargerRepo chargerRepo;
  private final ConnectionTypeToOcmMapper connectionTypeToOcmMapper =
      new ConnectionTypeToOcmMapper();
  private final AccessTypeToOcmMapper accessTypeToOcmMapper = new AccessTypeToOcmMapper();

  private final String STATUS_IDS = "30,75,100,150,200,210";

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

  public List<Charger> getChargersWithinRadius(Point point, Double radius) {
    return chargerRepo.findChargersWithinRadius(point, radius);
  }

  /**
   * Retrieves chargers based on various parameters.
   *
   * @param query the query object containing search parameters
   * @return a list of chargers matching the parameters
   */
  @LogExecutionTime(message = LogMessages.SQL_QUERY)
  public List<Charger> getChargersByParams(ChargerQuery query) {

    String connectionTypeIds = getConnectionTypeIdsAsString(query);

    String accessTypeIds = getAccessTypeIdsAsString(query);

    return chargerRepo.findChargersByParams(
        query.getPolygon(),
        query.getPoint(),
        query.getRadius(),
        connectionTypeIds,
        accessTypeIds,
        query.getMinKwChargeSpeed(),
        query.getMaxKwChargeSpeed(),
        query.getMinNoChargePoints(),
        query.getTopLeftLatLng(),
        query.getBottomRightLatLng());
  }

  private String getAccessTypeIdsAsString(ChargerQuery query) {
    List<Integer> mappedAccessTypeIds =
        accessTypeToOcmMapper.mapAccessTypeToDbIds(query.getAccessTypeIds());
    String accessTypeIds =
        String.join(
            ",", mappedAccessTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    return accessTypeIds;
  }

  private String getConnectionTypeIdsAsString(ChargerQuery query) {
    List<Integer> mappedConnectionTypeIds =
        connectionTypeToOcmMapper.mapConnectionTypeToDbIds(query.getConnectionTypeIds());
    String connectionTypeIds =
        String.join(
            ",",
            mappedConnectionTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);
    return connectionTypeIds;
  }

  /**
   * Retrieves charger locations based on various parameters.
   *
   * @param query the query object containing search parameters
   * @return a list of charger locations matching the parameters
   */
  @LogExecutionTime(message = LogMessages.SQL_QUERY)
  public List<Point> getChargerLocationsByParams(ChargerQuery query) {

    // function will not return duplicate points, should it?

    String connectionTypeIds = getConnectionTypeIdsAsString(query);

    String accessTypeIds = getAccessTypeIdsAsString(query);

    List<Object> projections =
        chargerRepo.findChargerLocationsByParams(
            query.getPolygon(),
            query.getPoint(),
            query.getRadius(),
            connectionTypeIds,
            accessTypeIds,
            query.getMinKwChargeSpeed(),
            query.getMaxKwChargeSpeed(),
            query.getMinNoChargePoints());

    return projections.stream()
        .map(projection -> (org.geolatte.geom.Point) projection)
        .map(GeolatteToPointConverter::convert)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the nearest charger based on various parameters.
   *
   * @param query the query object containing search parameters
   * @return the nearest charger matching the parameters
   */
  @LogExecutionTime(message = LogMessages.SQL_QUERY)
  public Charger getNearestChargerByParams(ChargerQuery query) {

    String connectionTypeIds = getConnectionTypeIdsAsString(query);

    String accessTypeIds = getAccessTypeIdsAsString(query);

    Charger nearestCharger =
        chargerRepo.findNearestChargerByParam(
            query.getPoint(),
            query.getRadius(),
            connectionTypeIds,
            accessTypeIds,
            query.getMinKwChargeSpeed(),
            query.getMaxKwChargeSpeed(),
            query.getMinNoChargePoints());

    return nearestCharger;
  }

  //  private String getConnectionTypeIds(ChargerQuery query) {
  //    String connectionTypeIds = getConnectionTypeIdsAsString(query);
  //    return connectionTypeIds;
  //  }

  /**
   * Retrieves the highest power connection by type in a charger.
   *
   * @param charger the charger entity
   * @param journey the route entity containing connection types
   * @return the highest power connection speed in kW
   */
  public Double getHighestPowerConnectionByTypeInCharger(Charger charger, Journey journey) {

    List<Integer> mappedConnectionTypeIds =
        connectionTypeToOcmMapper.mapConnectionTypeToDbIds(journey.getConnectionTypes());
    String connectionTypeIds =
        String.join(
            ",",
            mappedConnectionTypeIds.stream().map(String::valueOf).collect(Collectors.toList()));
    System.out.println(connectionTypeIds);

    Double chargeSpeed =
        chargerRepo.findHighestPowerConnectionByTypeInCharger(charger.getId(), connectionTypeIds);
    System.out.println("Highest power connection speed: " + chargeSpeed);
    return chargeSpeed;
  }

  //
  //  @PersistenceContext private EntityManager entityManager;
  //
  //  public List<Charger> findChargersByParam(
  //      Polygon polygon,
  //      Point point,
  //      Double radius,
  //      List<ConnectionType> connectionTypes,
  //      Integer minKwChargeSpeed,
  //      Integer maxKwChargeSpeed,
  //      Integer minNoChargePoints) {
  //    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
  //    CriteriaQuery<Charger> cq = cb.createQuery(Charger.class);
  //    Root<Charger> charger = cq.from(Charger.class);
  //    Join<Charger, Connection> connection = charger.join("connections", JoinType.INNER);
  //
  //    List<Predicate> predicates = new ArrayList<>();
  //
  //    // spatial params
  //    if (polygon != null) {
  //      predicates.add(
  //          cb.isTrue(
  //              cb.function(
  //                  "ST_Within", Boolean.class, charger.get("location"), cb.literal(polygon))));
  //    }
  //    if (point != null && radius != null) {
  //      predicates.add(
  //          cb.lessThanOrEqualTo(
  //              cb.function(
  //                  "ST_Distance_Sphere", Double.class, charger.get("location"),
  // cb.literal(point)),
  //              radius));
  //    }
  //
  //    // connection type IDs
  //    if (connectionTypes != null && !connectionTypes.isEmpty()) {
  //      List<Integer> connectionTypeIds =
  //          connectionTypeToOcmMapper.mapConnectionTypeToDbIds(connectionTypes);
  //      predicates.add(connection.get("connectionTypeID").in(connectionTypeIds));
  //    }
  //
  //    // min charge speed
  //    if (minKwChargeSpeed != null) {
  //      predicates.add(cb.greaterThanOrEqualTo(connection.get("powerKW"), minKwChargeSpeed));
  //    } else {
  //      predicates.add(cb.greaterThanOrEqualTo(connection.get("powerKW"), cb.literal(0)));
  //    }
  //
  //    // max charge speed
  //    if (maxKwChargeSpeed != null) {
  //      predicates.add(cb.lessThanOrEqualTo(connection.get("powerKW"), maxKwChargeSpeed));
  //    }
  //
  //    // min number of charge points
  //    predicates.add(
  //        cb.greaterThanOrEqualTo(
  //            charger.get("numberOfPoints"),
  //            cb.coalesce(cb.literal(minNoChargePoints), cb.literal(1))));
  //
  //    cq.where(cb.and(predicates.toArray(new Predicate[0])));
  //    return entityManager.createQuery(cq).getResultList();
  //  }

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

  @Deprecated
  public void deleteALLChargers() {
    chargerRepo.deleteAll();
  }

  @Transactional
  public void saveChargersInBatch(List<Charger> chargers) {
    List<Charger> failedChargers = new ArrayList<>();
    for (Charger charger : chargers) {
      charger.setCreatedAt(LocalDateTime.now());
      charger.setUpdatedAt(LocalDateTime.now());
      try {
        chargerRepo.save(charger);
      } catch (Exception e) {
        log.error("Failed to save charger ID {}: {}", charger.getId(), e.getMessage());
        failedChargers.add(charger);
      }
      log.info("Saved charger ID {}", charger.getId());
    }
    if (!failedChargers.isEmpty()) {
      handleFailedChargers(failedChargers);
    }
    log.info("Saved {} chargers in batch", chargers.size());
  }

  private void handleFailedChargers(List<Charger> failedChargers) {
    // TODO: implement this
    log.info("Handling {} failed chargers", failedChargers.size());
  }

  public void updateNullConnectionPowerKW() {
    int updatedCount = chargerRepo.updateNullPowerKW();
    System.out.println("Updated " + updatedCount + " connections where powerKW was null.");
  }
}
