package com.icl.fmfmc_backend.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.icl.fmfmc_backend.security.RateLimitFilter;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.PrintWriter;

@SpringBootTest
public class RateLimitFilterTest {

  @InjectMocks private RateLimitFilter rateLimitFilter;
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  @Mock private FilterChain mockChain;
  @Mock private PrintWriter mockWriter;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    when(mockResponse.getWriter()).thenReturn(mockWriter);
  }

  @Test
  void whenBucketIsNotAllowRequest() throws Exception {
    ConsumptionProbe probe = rateLimitFilter.getBucket().tryConsumeAndReturnRemaining(1);
    assertTrue(probe.isConsumed());

    rateLimitFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
    verify(mockResponse, never()).setStatus(429);
  }

  @Test
  void whenBucketIsEmptyThenBlockRequest() throws Exception {
    rateLimitFilter.getBucket().tryConsumeAndReturnRemaining(20);
    ConsumptionProbe probe = rateLimitFilter.getBucket().tryConsumeAndReturnRemaining(1);
    assertFalse(probe.isConsumed());

    rateLimitFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockChain, never()).doFilter(mockRequest, mockResponse);
    verify(mockResponse).setStatus(429);
    verify(mockWriter).write("Too many requests");
  }
}
