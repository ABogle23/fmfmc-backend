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

/** Repository interface for Charger entities. */
@Repository
public interface ChargerRepo extends JpaRepository<Charger, Long> {
  /**
   * Finds all chargers within a given polygon.
   *
   * @param polygon the polygon to search within
   * @return a list of chargers within the polygon
   */
  @Query(
      value = "SELECT * FROM {h-schema}chargers c WHERE ST_Within(c.location, :polygon) = true",
      nativeQuery = true)
  List<Charger> findAllWithinPolygon(@Param("polygon") Polygon polygon);

  /**
   * Finds chargers by connection type IDs.
   *
   * @param typeIds the list of connection type IDs
   * @return a list of chargers with the specified connection type IDs
   */
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

  /**
   * Finds chargers by connection charger speed.
   *
   * @param minKwSpeed the minimum kW charge speed
   * @param maxKwSpeed the maximum kW charge speed
   * @return a list of chargers with the specified charge speed range
   */
  @Query(
      "SELECT c FROM Charger c JOIN c.connections cc WHERE cc.powerKW >= COALESCE(:minKwChargeSpeed, 0)"
          + " AND (:maxKwChargeSpeed IS NULL OR cc.powerKW <= :maxKwChargeSpeed)")
  List<Charger> findByConnectionChargerSpeed(
      @Param("minKwChargeSpeed") Integer minKwSpeed, @Param("maxKwChargeSpeed") Integer maxKwSpeed);

  /**
   * Finds chargers by minimum number of charge points.
   *
   * @param minNoChargePoints the minimum number of charge points
   * @return a list of chargers with at least the specified number of charge points
   */
  @Query("SELECT c FROM Charger c  WHERE c.numberOfPoints >= COALESCE(:minNoChargePoints, 1)")
  List<Charger> findByMinNoChargePoints(@Param("minNoChargePoints") Integer minNoChargePoints);

  /**
   * Finds chargers within a given radius from a point.
   *
   * @param point the center point
   * @param radius the radius in meters
   * @return a list of chargers within the specified radius
   */
  @Query(
      value =
          "SELECT * FROM {h-schema}chargers c WHERE ST_Distance_Sphere(c.location, :point) <= :radius",
      nativeQuery = true)
  List<Charger> findChargersWithinRadius(
      @Param("point") Point point, @Param("radius") Double radius);

  /**
   * Finds chargers by various parameters.
   *
   * @param polygon the polygon to search within
   * @param point the center point
   * @param radius the radius in meters
   * @param connectionTypeIds the connection type IDs
   * @param accessTypeIds the access type IDs
   * @param minKwChargeSpeed the minimum kW charge speed
   * @param maxKwChargeSpeed the maximum kW charge speed
   * @param minNoChargePoints the minimum number of charge points
   * @param topLeftLatLng the top-left latitude and longitude
   * @param bottomRightLatLng the bottom-right latitude and longitude
   * @return a list of chargers matching the specified parameters
   */
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

  /**
   * Finds charger locations by various parameters.
   *
   * @param polygon the polygon to search within
   * @param point the center point
   * @param radius the radius in meters
   * @param connectionTypeIds the connection type IDs
   * @param accessTypeIds the access type IDs
   * @param minKwChargeSpeed the minimum kW charge speed
   * @param maxKwChargeSpeed the maximum kW charge speed
   * @param minNoChargePoints the minimum number of charge points
   * @return a list of charger locations matching the specified parameters
   */
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

  /**
   * Finds the nearest charger by various parameters.
   *
   * @param point the center point
   * @param radius the radius in meters
   * @param connectionTypeIds the connection type IDs
   * @param accessTypeIds the access type IDs
   * @param minKwChargeSpeed the minimum kW charge speed
   * @param maxKwChargeSpeed the maximum kW charge speed
   * @param minNoChargePoints the minimum number of charge points
   * @return the nearest charger matching the specified parameters
   */
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

  /**
   * Finds chargers within a bounding box.
   *
   * @param lat1 the latitude of the first corner of the bounding box
   * @param lon1 the longitude of the first corner of the bounding box
   * @param lat2 the latitude of the opposite corner of the bounding box
   * @param lon2 the longitude of the opposite corner of the bounding box
   * @param connectionTypeIds the connection type IDs
   * @param accessTypeIds the access type IDs
   * @param minKwChargeSpeed the minimum kW charge speed
   * @param maxKwChargeSpeed the maximum kW charge speed
   * @param minNoChargePoints the minimum number of charge points
   * @return a list of chargers within the bounding box
   */
  @Query(
      value =
          """
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
      @Param("lat1") Double lat1,
      @Param("lon1") Double lon1,
      @Param("lat2") Double lat2,
      @Param("lon2") Double lon2,
      @Param("connectionTypeIds") String connectionTypeIds,
      @Param("accessTypeIds") String accessTypeIds,
      @Param("minKwChargeSpeed") Integer minKwChargeSpeed,
      @Param("maxKwChargeSpeed") Integer maxKwChargeSpeed,
      @Param("minNoChargePoints") Integer minNoChargePoints);

  /**
   * Finds the highest power connection by type in a charger.
   *
   * @param chargerId the ID of the charger
   * @param connectionTypeIds the connection type IDs
   * @return the highest power connection in kW
   */
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

  /**
   * Updates the power kW of charger connections to 3 where it is null.
   *
   * @return the number of rows affected
   */
  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE {h-schema}charger_connections SET powerkw = 3 WHERE powerkw IS NULL AND charger_id IN (SELECT id FROM {h-schema}chargers)",
      nativeQuery = true)
  int updateNullPowerKW();
}
