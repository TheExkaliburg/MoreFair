package de.kaliburg.morefair.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

public class CustomCookieCsrfTokenRepository implements CsrfTokenRepository {

  private final CsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

  @Override
  public CsrfToken generateToken(HttpServletRequest request) {
    return csrfTokenRepository.generateToken(request);
  }

  @Override
  public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
    String tokenValue = token != null ? token.getToken() : "";
    response.addHeader("Set-Cookie", "XSRF-TOKEN=" + tokenValue + "; Path=/; "
        + "SameSite=Strict; Secure");

  }

  @Override
  public CsrfToken loadToken(HttpServletRequest request) {
    return csrfTokenRepository.loadToken(request);
  }
}
