package com.icl.fmfmc_backend.config;

import com.icl.fmfmc_backend.security.ApiKeyAuthenticationFilter;
import com.icl.fmfmc_backend.security.ApiKeyAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

  private final FmfmcApiKeyProperties fmfmcApiKeyProperties;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/**").authenticated().anyRequest().permitAll())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(form -> form.disable())
        .requiresChannel(channel -> channel.requestMatchers("/api/**").requiresSecure())
        .headers(
            headers ->
                headers.httpStrictTransportSecurity(
                    hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))) // Enable HSTS

        //                .addFilterBefore(apiKeyFilter(), BasicAuthenticationFilter.class);
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
}
