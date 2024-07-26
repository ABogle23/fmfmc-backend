package com.icl.fmfmc_backend.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
  private final String apiKey;

  public ApiKeyAuthenticationToken(String apiKey) {
    super(null);
    this.apiKey = apiKey;
    setAuthenticated(false);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return this.apiKey;
  }
}
