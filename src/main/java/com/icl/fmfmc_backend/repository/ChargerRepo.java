package com.icl.fmfmc_backend.repository;

import com.icl.fmfmc_backend.entity.Charger;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ChargerRepo extends JpaRepository<Charger, Long> {
    @Query(value = "SELECT * FROM fmfmc_db.chargers c WHERE ST_Within(c.location, :polygon) = true", nativeQuery = true)
    List<Charger> findAllWithinPolygon(@Param("polygon") Polygon polygon);

}
