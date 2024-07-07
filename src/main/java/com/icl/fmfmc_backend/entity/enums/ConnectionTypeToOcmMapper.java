package com.icl.fmfmc_backend.entity.enums;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConnectionTypeToOcmMapper {

  private static final Map<ConnectionType, Integer[]> connectionTypeToOcm =
      new EnumMap<>(ConnectionType.class);

  static {
    connectionTypeToOcm.put(ConnectionType.TYPE2, new Integer[]{25,1036});
    connectionTypeToOcm.put(ConnectionType.CHADEMO, new Integer[]{2});
    connectionTypeToOcm.put(ConnectionType.CCS, new Integer[]{33});
    connectionTypeToOcm.put(ConnectionType.TESLA, new Integer[]{27,30});
    connectionTypeToOcm.put(ConnectionType.DOMESTIC_3_PIN, new Integer[]{3});
    connectionTypeToOcm.put(ConnectionType.TYPE1, new Integer[]{1});
  }

  public List<Integer> mapConnectionTypeToDbIds(List<ConnectionType> types) {
    if (types == null) {
      return Collections.emptyList(); // ret empty list if types is null
    }
    List<Integer> dbIds = new ArrayList<>();
    for (ConnectionType type : types) {
      Integer[] ids = connectionTypeToOcm.get(type);
      if (ids != null) {
        dbIds.addAll(Arrays.asList(ids));
      }
    }
    return dbIds;
  }
}
