package com.icl.fmfmc_backend.security;

import com.icl.fmfmc_backend.config.FmfmcApiKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@RequiredArgsConstructor
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

  private final FmfmcApiKeyProperties fmfmcApiKeyProperties;

  //    private final String VALID_API_KEY = fmfmcApiKeyProperties.getApiKey();

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String VALID_API_KEY = fmfmcApiKeyProperties.getApiKey();

    String apiKey = (String) authentication.getPrincipal();
    if (VALID_API_KEY.equals(apiKey)) {
      Authentication auth = new ApiKeyAuthenticationToken(apiKey);
      auth.setAuthenticated(true);
      return auth;
    }
    throw new AuthenticationException("Authentication failed") {};
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return ApiKeyAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
