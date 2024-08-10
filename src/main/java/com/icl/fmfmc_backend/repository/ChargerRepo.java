package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.charger.Charger;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChargerRepo extends JpaRepository<Charger, Long> {
  @Query(
      value = "SELECT * FROM {h-schema}chargers c WHERE ST_Within(c.location, :polygon) = true",
      nativeQuery = true)
  List<Charger> findAllWithinPolygon(@Param("polygon") Polygon polygon);

  @Query("SELECT c FROM Charger c JOIN c.connections cc WHERE cc.connectionTypeID IN :typeIds")
  //  @Query(
  //      value =
  //          """
  //                      SELECT DISTINCT c.* FROM {h-schema}chargers c\s
  //                      JOIN {h-schema}charger_connections cc ON c.id = cc.charger_id\s
  //                      WHERE cc.connection_typeid IN (:typeIds)""",
  //      nativeQuery = true)
  List<Charger> findByConnectionTypeIds(@Param("typeIds") List<Integer> typeIds);

  //    @Query("SELECT c FROM Charger c JOIN c.connections cc WHERE cc.powerKW BETWEEN
  // :minKwChargeSpeed AND :maxKwChargeSpeed")
  //    List<Charger> findByConnectionChargerSpeed(@Param("minKwChargeSpeed")Integer minKwSpeed,
  // @Param("maxKwChargeSpeed")Integer maxKwSpeed);

  @Query(
      "SELECT c FROM Charger c JOIN c.connections cc WHERE cc.powerKW >= COALESCE(:minKwChargeSpeed, 0)"
          + " AND (:maxKwChargeSpeed IS NULL OR cc.powerKW <= :maxKwChargeSpeed)")
  List<Charger> findByConnectionChargerSpeed(
      @Param("minKwChargeSpeed") Integer minKwSpeed, @Param("maxKwChargeSpeed") Integer maxKwSpeed);

  @Query("SELECT c FROM Charger c  WHERE c.numberOfPoints >= COALESCE(:minNoChargePoints, 1)")
  List<Charger> findByMinNoChargePoints(@Param("minNoChargePoints") Integer minNoChargePoints);

  @Query(
      value =
          "SELECT * FROM {h-schema}chargers c WHERE ST_Distance_Sphere(c.location, :point) <= :radius",
      nativeQuery = true)
  List<Charger> findChargersWithinRadius(
      @Param("point") Point point, @Param("radius") Double radius);

  @Query(
      value =
          """
               SELECT DISTINCT c.* FROM {h-schema}chargers c
               JOIN {h-schema}charger_connections cc ON c.id = cc.charger_id
               WHERE
               (:polygon IS NULL OR ST_Within(c.location, :polygon) = TRUE)
               AND ((:topLeftLatLng IS NULL OR :bottomRightLatLng IS NULL) OR ST_Within(c.location, ST_MakeEnvelope(:topLeftLatLng, :bottomRightLatLng)) = TRUE)
               AND (:point IS NULL OR :radius IS NULL OR ST_Distance_Sphere(c.location, :point) <= :radius)
               AND ((:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
               AND (COALESCE(:minKwChargeSpeed, 0) = 0 OR cc.powerkw >= :minKwChargeSpeed)
               AND (cc.status_typeid NOT IN (30, 75, 100, 150, 200, 210))
               AND (cc.status_typeid IS NOT NULL)
               AND (:maxKwChargeSpeed IS NULL OR cc.powerkw <= :maxKwChargeSpeed))
               AND c.number_of_points >= COALESCE(:minNoChargePoints, 1)
               AND (:accessTypeIds IS NULL OR :accessTypeIds = '' OR FIND_IN_SET(c.usage_typeid, :accessTypeIds) > 0)
               """,
      nativeQuery = true)
  List<Charger> findChargersByParams(
      @Param("polygon") Polygon polygon,
      @Param("point") Point point,
      @Param("radius") Double radius,
      @Param("connectionTypeIds") String connectionTypeIds,
      @Param("accessTypeIds") String accessTypeIds,
      @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
      @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
      @Param("minNoChargePoints") Integer minNoChargePoints,
      @Param("topLeftLatLng") Point topLeftLatLng,
      @Param("bottomRightLatLng") Point bottomRightLatLng);

  @Query(
      value =
          """
               SELECT DISTINCT c.location FROM {h-schema}chargers c
               JOIN {h-schema}charger_connections cc ON c.id = cc.charger_id
               WHERE
               (:polygon IS NULL OR ST_Within(c.location, :polygon) = TRUE)
               AND (:point IS NULL OR :radius IS NULL OR ST_Distance_Sphere(c.location, :point) <= :radius)
               AND ((:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
               AND (COALESCE(:minKwChargeSpeed, 0) = 0 OR cc.powerkw >= :minKwChargeSpeed)
               AND (cc.status_typeid NOT IN (30, 75, 100, 150, 200, 210))
               AND (cc.status_typeid IS NOT NULL)
               AND (:maxKwChargeSpeed IS NULL OR cc.powerkw <= :maxKwChargeSpeed))
               AND c.number_of_points >= COALESCE(:minNoChargePoints, 1)
               AND (:accessTypeIds IS NULL OR :accessTypeIds = '' OR FIND_IN_SET(c.usage_typeid, :accessTypeIds) > 0)
               """,
      nativeQuery = true)
  List<Object> findChargerLocationsByParams(
      @Param("polygon") Polygon polygon,
      @Param("point") Point point,
      @Param("radius") Double radius,
      @Param("connectionTypeIds") String connectionTypeIds,
      @Param("accessTypeIds") String accessTypeIds,
      @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
      @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
      @Param("minNoChargePoints") Integer minNoChargePoints);

  @Query(
      value =
          """
             SELECT c.* FROM {h-schema}chargers c
             JOIN {h-schema}charger_connections cc ON c.id = cc.charger_id
             WHERE
             (:point IS NOT NULL AND :radius IS NOT NULL AND ST_Distance_Sphere(c.location, :point) <= :radius)
             AND ((:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
             AND (COALESCE(:minKwChargeSpeed, 0) = 0 OR cc.powerkw >= :minKwChargeSpeed)
             AND (cc.status_typeid NOT IN (30, 75, 100, 150, 200, 210))
             AND (cc.status_typeid IS NOT NULL)
             AND (:maxKwChargeSpeed IS NULL OR cc.powerkw <= :maxKwChargeSpeed))
             AND c.number_of_points >= COALESCE(:minNoChargePoints, 1)
             AND (:accessTypeIds IS NULL OR :accessTypeIds = '' OR FIND_IN_SET(c.usage_typeid, :accessTypeIds) > 0)
             ORDER BY ST_Distance_Sphere(c.location, :point) ASC
             LIMIT 1
             """,
      nativeQuery = true)
  Charger findNearestChargerByParam(
      @Param("point") Point point,
      @Param("radius") Double radius,
      @Param("connectionTypeIds") String connectionTypeIds,
      @Param("accessTypeIds") String accessTypeIds,
      @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
      @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
      @Param("minNoChargePoints") Integer minNoChargePoints);


  @Query(
          value = """
            SELECT DISTINCT c.* FROM {h-schema}chargers c
            JOIN {h-schema}charger_connections cc ON c.id = cc.charger_id
            WHERE
            (ST_Within(c.location, ST_MakeEnvelope(:lon1, :lat1, :lon2, :lat2, 4326)) = TRUE)
            AND ((:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
            AND (COALESCE(:minKwChargeSpeed, 0) = 0 OR cc.powerkw >= :minKwChargeSpeed)
            AND (cc.status_typeid NOT IN (30, 75, 100, 150, 200, 210))
            AND (cc.status_typeid IS NOT NULL)
            AND (:maxKwChargeSpeed IS NULL OR cc.powerkw <= :maxKwChargeSpeed))
            AND c.number_of_points >= COALESCE(:minNoChargePoints, 1)
            AND (:accessTypeIds IS NULL OR :accessTypeIds = '' OR FIND_IN_SET(c.usage_typeid, :accessTypeIds) > 0)
            """,
          nativeQuery = true)
  List<Charger> findChargersInBoundingBox(
          @Param("lat1") Double lat1, @Param("lon1") Double lon1,
          @Param("lat2") Double lat2, @Param("lon2") Double lon2,
          @Param("connectionTypeIds") String connectionTypeIds,
          @Param("accessTypeIds") String accessTypeIds,
          @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
          @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
          @Param("minNoChargePoints") Integer minNoChargePoints);


  @Query(
      value =
          """
               SELECT cc.powerkw FROM {h-schema}charger_connections cc
               WHERE cc.charger_id = :chargerId
               AND (:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
               AND cc.status_typeid NOT IN (30, 75, 100, 150, 200, 210)
               AND cc.status_typeid IS NOT NULL
               ORDER BY cc.powerkw DESC
               LIMIT 1
               """,
      nativeQuery = true)
  Double findHighestPowerConnectionByTypeInCharger(
      @Param("chargerId") Long chargerId, @Param("connectionTypeIds") String connectionTypeIds);

  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE {h-schema}charger_connections SET powerkw = 3 WHERE powerkw IS NULL AND charger_id IN (SELECT id FROM {h-schema}chargers)",
      nativeQuery = true)
  int updateNullPowerKW();
}
