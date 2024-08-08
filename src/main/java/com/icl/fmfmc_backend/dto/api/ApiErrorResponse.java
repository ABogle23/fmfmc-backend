package com.icl.fmfmc_backend.dto.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

// generic wrapper class for error response to client.
@Data
@RequiredArgsConstructor
public class ApiErrorResponse {
  private final int status;
  private final String message;
  private final List<String> details;
}
