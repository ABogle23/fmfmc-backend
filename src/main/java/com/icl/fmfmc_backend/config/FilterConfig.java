package com.icl.fmfmc_backend.config;

import com.icl.fmfmc_backend.security.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/** Configuration class for registering servlet filters. */
@Component
// @Profile("!test")
public class FilterConfig {

  /**
   * Registers the RateLimitFilter for the application.
   *
   * @return a FilterRegistrationBean configured with the RateLimitFilter
   */
  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RateLimitFilter());
    registration.addUrlPatterns("/api/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
