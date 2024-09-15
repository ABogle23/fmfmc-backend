package com.icl.fmfmc_backend.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The ideal segment of the route to stop for a food establishment")
public enum StoppingRange {
  earliest,
  early,
  middle,
  later,
  latest,

  // hidden from the client
  extendedEarly,
  extendedMiddle,
  extendedLater,

}
