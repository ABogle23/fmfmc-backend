package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.Charger.Charger;
import com.icl.fmfmc_backend.entity.Charger.Connection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargerRepo extends JpaRepository<Charger, Long> {
  @Query(
      value = "SELECT * FROM fmfmc_db.chargers c WHERE ST_Within(c.location, :polygon) = true",
      nativeQuery = true)
  List<Charger> findAllWithinPolygon(@Param("polygon") Polygon polygon);

  @Query("SELECT c FROM Charger c JOIN c.connections cc WHERE cc.connectionTypeID IN :typeIds")
  //  @Query(
  //      value =
  //          """
  //                      SELECT DISTINCT c.* FROM fmfmc_db.chargers c\s
  //                      JOIN fmfmc_db.charger_connections cc ON c.id = cc.charger_id\s
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
          "SELECT * FROM fmfmc_db.chargers c WHERE ST_Distance_Sphere(c.location, :point) <= :radius",
      nativeQuery = true)
  List<Charger> findChargersWithinRadius(
      @Param("point") Point point, @Param("radius") Double radius);


  @Query(value = """
               SELECT DISTINCT c.* FROM fmfmc_db.chargers c
               JOIN fmfmc_db.charger_connections cc ON c.id = cc.charger_id
               WHERE 
               (:polygon IS NULL OR ST_Within(c.location, :polygon) = TRUE)
               AND (:point IS NULL OR :radius IS NULL OR ST_Distance_Sphere(c.location, :point) <= :radius)
               AND ((:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
               AND (COALESCE(:minKwChargeSpeed, 0) = 0 OR cc.powerkw >= :minKwChargeSpeed)
               AND (:maxKwChargeSpeed IS NULL OR cc.powerkw <= :maxKwChargeSpeed))
               AND c.number_of_points >= COALESCE(:minNoChargePoints, 1)
               AND (:accessTypeIds IS NULL OR :accessTypeIds = '' OR FIND_IN_SET(c.usage_typeid, :accessTypeIds) > 0)
               """, nativeQuery = true)
  List<Charger> findChargersByParams(
          @Param("polygon") Polygon polygon,
          @Param("point") Point point,
          @Param("radius") Double radius,
          @Param("connectionTypeIds") String connectionTypeIds,
          @Param("accessTypeIds") String accessTypeIds,
          @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
          @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
          @Param("minNoChargePoints") Integer minNoChargePoints);

  @Query(value = """
               SELECT cc.powerkw FROM fmfmc_db.charger_connections cc
               WHERE cc.charger_id = :chargerId
               AND (:connectionTypeIds IS NULL OR :connectionTypeIds = '' OR FIND_IN_SET(cc.connection_typeid, :connectionTypeIds) > 0)
               ORDER BY cc.powerkw DESC
               LIMIT 1
               """, nativeQuery = true)
  Double findHighestPowerConnectionByTypeInCharger(
          @Param("chargerId") Long chargerId,
          @Param("connectionTypeIds") String connectionTypeIds);

}
