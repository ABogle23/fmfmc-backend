package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.Charger;
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
}
