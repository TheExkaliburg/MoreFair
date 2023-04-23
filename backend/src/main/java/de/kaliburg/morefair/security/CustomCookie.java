package de.kaliburg.morefair.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.server.Cookie.SameSite;


public class CustomCookie extends Cookie {

  @Getter
  @Setter
  private SameSite sameSite;

  public CustomCookie(String name, String value) {
    super(name, value);
  }

  public static CustomCookie fromCookie(Cookie cookie) {
    CustomCookie customCookie = new CustomCookie(cookie.getName(), cookie.getValue());
    customCookie.setComment(cookie.getComment());
    customCookie.setDomain(cookie.getDomain());
    customCookie.setHttpOnly(cookie.isHttpOnly());
    customCookie.setMaxAge(cookie.getMaxAge());
    customCookie.setPath(cookie.getPath());
    customCookie.setSecure(cookie.getSecure());
    customCookie.setVersion(cookie.getVersion());
    return customCookie;
  }

  public static CustomCookie[] fromCookies(Cookie[] cookies) {
    CustomCookie[] customCookies = new CustomCookie[cookies.length];
    for (int i = 0; i < cookies.length; i++) {
      customCookies[i] = fromCookie(cookies[i]);
    }
    return customCookies;
  }

  public static CustomCookie[] fromServletRequest(HttpServletRequest request) {
    return fromCookies(request.getCookies());
  }

  public String toHeaderString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName()).append("=").append(getValue());
    if (getDomain() != null) {
      sb.append("; Domain=").append(getDomain());
    }
    if (getPath() != null) {
      sb.append("; Path=").append(getPath());
    }
    if (getMaxAge() >= 0) {
      sb.append("; Max-Age=").append(getMaxAge());
    }
    if (getSecure()) {
      sb.append("; Secure");
    }
    if (isHttpOnly()) {
      sb.append("; HttpOnly");
    }
    if (getSameSite() != null) {
      sb.append("; SameSite=").append(getSameSite().toString());
    }
    return sb.toString();
  }


}
