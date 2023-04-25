package de.kaliburg.morefair.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.util.StringUtils;

public class CustomCookieCsrfTokenRepository implements CsrfTokenRepository {

  private static final String CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME =
      CookieCsrfTokenRepository.class.getName().concat(".REMOVED");
  private final CookieCsrfTokenRepository csrfTokenRepository =
      CookieCsrfTokenRepository.withHttpOnlyFalse();

  @Override
  public CsrfToken generateToken(HttpServletRequest request) {
    return csrfTokenRepository.generateToken(request);
  }

  @Override
  public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
    String tokenValue = token != null ? token.getToken() : "";
    CustomCookie cookie = new CustomCookie("XSRF-TOKEN", tokenValue);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(-1);
    cookie.setHttpOnly(false);
    cookie.setSameSite(SameSite.STRICT);
    response.addHeader("Set-Cookie", cookie.toHeaderString());

    //response.addHeader("Set-Cookie", "XSRF-TOKEN=" + tokenValue + "; Path=/; "
    //    + "SameSite=Strict; Secure");

    if (!StringUtils.hasLength(tokenValue)) {
      request.setAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME, Boolean.TRUE);
    } else {
      request.removeAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME);
    }
    response.addHeader("X-ADDED-XSRF-COOKIE", "true");
  }

  @Override
  public CsrfToken loadToken(HttpServletRequest request) {
    return csrfTokenRepository.loadToken(request);
  }
}
