package com.icl.fmfmc_backend.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

/** A filter that applies rate limiting to incoming requests. */
public class RateLimitFilter implements jakarta.servlet.Filter {

  private Bucket bucket;

  /** Constructs a RateLimitFilter with a default rate limit configuration. */
  public RateLimitFilter() {
    // Configure the rate limit
    Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
    this.bucket = Bucket4j.builder().addLimit(limit).build();
  }

  /**
   * Initializes the filter with a specific rate limit configuration.
   *
   * @param filterConfig the filter configuration
   * @throws jakarta.servlet.ServletException if an error occurs during initialization
   */
  @Override
  public void init(jakarta.servlet.FilterConfig filterConfig)
      throws jakarta.servlet.ServletException {
    // limit of 20 tokens per minute
    Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
    this.bucket = Bucket4j.builder().addLimit(limit).build();
  }

  /**
   * Applies rate limiting to the incoming request.
   *
   * @param servletRequest the servlet request
   * @param servletResponse the servlet response
   * @param filterChain the filter chain
   * @throws IOException if an I/O error occurs during filtering
   * @throws jakarta.servlet.ServletException if an error occurs during filtering
   */
  @Override
  public void doFilter(
      jakarta.servlet.ServletRequest servletRequest,
      jakarta.servlet.ServletResponse servletResponse,
      jakarta.servlet.FilterChain filterChain)
      throws IOException, jakarta.servlet.ServletException {
    System.out.println("Rate Limiting");
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (probe.isConsumed()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      jakarta.servlet.http.HttpServletResponse httpResponse =
          (jakarta.servlet.http.HttpServletResponse) servletResponse;
      httpResponse.setContentType("text/plain");
      httpResponse.setStatus(429);
      httpResponse.getWriter().write("Too many requests");
    }
  }

  @Override
  public void destroy() {}

  public Bucket getBucket() {
    return bucket;
  }
}
