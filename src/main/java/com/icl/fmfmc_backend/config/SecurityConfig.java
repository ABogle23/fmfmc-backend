package com.icl.fmfmc_backend.config;

import com.icl.fmfmc_backend.config.integration.FmfmcApiKeyProperties;
import com.icl.fmfmc_backend.security.ApiKeyAuthenticationFilter;
import com.icl.fmfmc_backend.security.ApiKeyAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration class for the application.
 *
 * <p>This class configures security settings such as CORS, CSRF, session management, and
 * authentication mechanisms for the application.
 */
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
// @Profile("!test")
public class SecurityConfig {

  private final FmfmcApiKeyProperties fmfmcApiKeyProperties;

  //  private final CorsConfigurationSource corsConfigurationSource;

  /**
   * Configures the security filter chain.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        //            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .cors(
            cors ->
                cors.configurationSource(
                    request -> {
                      CorsConfiguration config = new CorsConfiguration();
                      config.setAllowedOrigins(Arrays.asList("http://localhost:63342"));
                      config.setAllowedMethods(
                          Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                      config.setAllowedHeaders(Arrays.asList("*"));
                      config.setAllowCredentials(true);
                      return config;
                    }))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/**").authenticated().anyRequest().permitAll())
        .exceptionHandling(
            e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(form -> form.disable())

                .requiresChannel(channel -> channel.requestMatchers("/api/**").requiresSecure())
                .headers(
                    headers ->
                        headers.httpStrictTransportSecurity(
                            hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
        // enable hsts
        .addFilterBefore(apiKeyFilter(), BasicAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public ApiKeyAuthenticationFilter apiKeyFilter() {
    return new ApiKeyAuthenticationFilter(authenticationManager());
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(new ApiKeyAuthenticationProvider(fmfmcApiKeyProperties));
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:63342"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
