package de.kaliburg.morefair.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    csrfTokenRepository.saveToken(token, request, response);
    // get the exisiting cookie from the response for the XSRF-TOKEN and change the sameSite
    // attribute to strict

  }

  @Override
  public CsrfToken loadToken(HttpServletRequest request) {
    return csrfTokenRepository.loadToken(request);
  }
}
