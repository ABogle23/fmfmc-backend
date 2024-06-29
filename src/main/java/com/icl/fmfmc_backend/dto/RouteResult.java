package com.icl.fmfmc_backend.dto;

import com.icl.fmfmc_backend.entity.Charger;
import com.icl.fmfmc_backend.entity.FoodEstablishment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.RouteMatcher;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

  private String routePolyline;

  private Double distance;

  private Double time; // mins

  private List<Charger> chargers;

  private List<FoodEstablishment> foodEstablishments;

  private RouteRequest context; // original request
}
