package de.kaliburg.morefair.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class CustomPersistentTokenBasedRememberMeServices extends
    PersistentTokenBasedRememberMeServices {

  private Boolean useSecureCookie;
  @Getter
  private String cookieDomain;

  public CustomPersistentTokenBasedRememberMeServices(String key,
      UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
    super(key, userDetailsService, tokenRepository);
  }


  @Override
  protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request,
      HttpServletResponse response) {
    String cookieValue = this.encodeCookie(tokens);
    CustomCookie cookie = new CustomCookie(this.getCookieName(), cookieValue);
    cookie.setMaxAge(maxAge);
    cookie.setPath(this.getCookiePath(request));

    if (this.cookieDomain != null) {
      cookie.setDomain(this.cookieDomain);
    }
    if (maxAge < 1) {
      cookie.setVersion(1);
    }

    cookie.setSecure(this.useSecureCookie != null ? this.useSecureCookie : request.isSecure());
    cookie.setHttpOnly(true);
    cookie.setSameSite(SameSite.LAX);
    response.addHeader("Set-Cookie", cookie.toHeaderString());
  }

  protected String getCookiePath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    return contextPath.length() > 0 ? contextPath : "/";
  }

  @Override
  public void setCookieDomain(String cookieDomain) {
    super.setCookieDomain(cookieDomain);
    this.cookieDomain = cookieDomain;
  }

  @Override
  public void setUseSecureCookie(boolean useSecureCookie) {
    super.setUseSecureCookie(useSecureCookie);
    this.useSecureCookie = useSecureCookie;
  }
}
