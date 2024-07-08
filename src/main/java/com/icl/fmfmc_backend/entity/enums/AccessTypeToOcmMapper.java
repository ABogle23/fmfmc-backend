package com.icl.fmfmc_backend.entity.enums;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class AccessTypeToOcmMapper {

  private static final Map<AccessType, Integer[]> accessTypeToOcm =
      new EnumMap<>(AccessType.class);

  static {
    accessTypeToOcm.put(AccessType.PUBLIC, new Integer[]{1,4,5,7});
    accessTypeToOcm.put(AccessType.RESTRICTED, new Integer[]{6});
    accessTypeToOcm.put(AccessType.PRIVATE, new Integer[]{2,3});
  }

  public List<Integer> mapAccessTypeToDbIds(List<AccessType> types) {
    if (types == null) {
      return Collections.emptyList(); // ret empty list if types is null
    }
    List<Integer> dbIds = new ArrayList<>();
    for (AccessType type : types) {
      Integer[] ids = accessTypeToOcm.get(type);
      if (ids != null) {
        dbIds.addAll(Arrays.asList(ids));
      }
    }
    return dbIds;
  }
}
