package com.icl.fmfmc_backend.dto.Api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private boolean success;
    private String message;
    private boolean fallbackUsed;
    private String fallbackDetails;

    public ApiResponse(T data, boolean success, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.fallbackUsed = false;
        this.fallbackDetails = null;
    }

    public ApiResponse(T data, boolean success, String message, boolean fallbackUsed, String fallbackDetails) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.fallbackUsed = fallbackUsed;
        this.fallbackDetails = fallbackDetails;
    }
}
