package com.icl.fmfmc_backend.util;

import java.util.Map;

public class JsonPayloadBuilder {

  public static String buildJsonPayload(Map<String, Object> replacements) {
    String jsonTemplate =
        """
                {
                  "start_lat": 51.107673,
                  "start_long": -2.110748,
                  "end_lat": 51.460065,
                  "end_long": -1.117859,
                  "starting_battery": 0.9,
                  "ev_range": 100000,
                  "battery_capacity": 100.0,
                  "min_charge_level": 0.3,
                  "charge_level_after_each_stop": 0.9,
                  "final_destination_charge_level": 0.5,
                  "connection_types": null,
                  "access_types": null,
                  "min_kw_charge_speed": null,
                  "max_kw_charge_speed": null,
                  "min_no_charge_points": 1,
                  "stop_for_eating": false,
                  "min_price": 2,
                  "max_price": 4,
                  "max_walking_distance": 500,
                  "include_alternative_eating_options": false,
                  "depart_time": "14:46:24.700000",
                  "meal_time": "00:00:00.000000",
                  "break_duration": "00:01:00.000000",
                  "stopping_range": "middle",
                  "charger_search_deviation": "minimal",
                  "eating_option_search_deviation": "minimal"
                }
                """;

    for (Map.Entry<String, Object> entry : replacements.entrySet()) {
      String placeholder = "\"" + entry.getKey() + "\": " + getJsonValue(entry.getValue());
      jsonTemplate = jsonTemplate.replaceAll("\"" + entry.getKey() + "\": [^,\\n]*", placeholder);
    }

    return jsonTemplate;
  }

  private static String getJsonValue(Object value) {
    if (value instanceof String) {
      return "\"" + value + "\"";
    } else {
      return value.toString();
    }
  }
}
