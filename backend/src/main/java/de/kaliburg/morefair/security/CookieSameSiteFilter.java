package de.kaliburg.morefair.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieSameSiteFilter implements Filter {

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

    boolean iFramed = Boolean.parseBoolean(httpServletRequest.getHeader("is-iframed"));

    if (iFramed) {
      // Allow iframe access
      Collection<String> setCookieStrings = httpServletResponse.getHeaders("Set-Cookie");
      List<String> newCookies = new ArrayList<>();

      if (setCookieStrings.size() > 0) {
        for (String setCookieString : setCookieStrings) {
          newCookies.add(removeSameSite(setCookieString));
        }

        httpServletResponse.setHeader("Set-Cookie", newCookies.get(0));
        for (int i = 1; i < newCookies.size(); i++) {
          httpServletResponse.addHeader("Set-Cookie", newCookies.get(i));
        }
        log.info("Set-Cookie header changed to: " + newCookies.toString());
      }
    }

    if(filterChain != null) {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  private String removeSameSite(String setCookieString) {
    String newCookieString = setCookieString.replace("SameSite=Lax", "SameSite=None");
    newCookieString = newCookieString.replace("SameSite=LAX", "SameSite=None");
    newCookieString = newCookieString.replace("SameSite=lax", "SameSite=None");
    newCookieString = newCookieString.replace("SameSite=Strict", "SameSite=None");
    newCookieString = newCookieString.replace("SameSite=STRICT", "SameSite=None");
    newCookieString = newCookieString.replace("SameSite=strict", "SameSite=None");
    return newCookieString;
  }
}
