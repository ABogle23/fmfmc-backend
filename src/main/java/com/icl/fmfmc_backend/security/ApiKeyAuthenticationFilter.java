package com.icl.fmfmc_backend.security;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
  private final AuthenticationManager authenticationManager;

  public ApiKeyAuthenticationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  //    @Override
  //    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
  // FilterChain chain) throws IOException, ServletException {
  //        String apiKey = request.getHeader("X-API-Key");
  //        if (apiKey != null) {
  //            try {
  //                ApiKeyAuthenticationToken authRequest = new ApiKeyAuthenticationToken(apiKey);
  //                Authentication authResult = authenticationManager.authenticate(authRequest);
  //                SecurityContextHolder.getContext().setAuthentication(authResult);
  //            } catch (AuthenticationException e) {
  //                SecurityContextHolder.clearContext();
  //                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication
  // Failed");
  //                return;
  //            }
  //        }
  //        chain.doFilter(request, response);
  //    }

  /**
   * Filters incoming requests and attempts to authenticate them using an API key.
   *
   * @param request the HttpServletRequest
   * @param response the HttpServletResponse
   * @param filterChain the FilterChain
   * @throws ServletException if an error occurs during filtering
   * @throws IOException if an I/O error occurs during filtering
   */
  @Override
  protected void doFilterInternal(
      jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response,
      jakarta.servlet.FilterChain filterChain)
      throws jakarta.servlet.ServletException, IOException {
    String apiKey = request.getHeader("X-API-Key");
    logger.info("API Key filtering");
    if (apiKey != null) {
      try {
        ApiKeyAuthenticationToken authRequest = new ApiKeyAuthenticationToken(apiKey);
        Authentication authResult = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        logger.info("Authentication successful");
      } catch (AuthenticationException e) {
        SecurityContextHolder.clearContext();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
        logger.error("Authentication failed");
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
