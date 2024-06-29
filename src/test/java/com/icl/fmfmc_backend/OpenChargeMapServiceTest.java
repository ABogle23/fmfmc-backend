package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.config.OpenChargeMapProperties;
import com.icl.fmfmc_backend.entity.Charger;
import com.icl.fmfmc_backend.service.ChargerService;
import com.icl.fmfmc_backend.service.OpenChargeMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenChargeMapServiceTest {}
