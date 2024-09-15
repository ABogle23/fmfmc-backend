package com.icl.fmfmc_backend.entity.enums;

public class EnumUtils {
  public static <E extends Enum<E> & FmfmcEnum> E getEnumFromApiName(
      Class<E> enumClass, String apiName) {
    for (E enumConstant : enumClass.getEnumConstants()) {
      if (enumConstant.getApiName().equalsIgnoreCase(apiName)) {
        return enumConstant;
      }
    }
    throw new IllegalArgumentException(
        "No constant with apiName " + apiName + " found in " + enumClass.getSimpleName());
  }
}
