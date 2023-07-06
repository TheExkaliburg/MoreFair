package de.kaliburg.morefair.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Slf4j
public class CustomCookieSerializer extends DefaultCookieSerializer {

  @Override
  public void writeCookieValue(CookieValue cookieValue) {
    super.writeCookieValue(cookieValue);

    HttpServletResponse response = cookieValue.getResponse();
    HttpServletRequest request = cookieValue.getRequest();

    try {
      new CookieSameSiteFilter().doFilter(request, response, null);
    }catch (Exception e) {
      log.error("Cookie SameSite attribute could not be set", e);
    }

    Collection<String> headers = response.getHeaders("Set-Cookie");
  }
}
