package de.kaliburg.morefair.security;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
public class SessionAttributesFilter extends GenericFilterBean {

  private final AccountService accountService;

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpSession session = request.getSession();

    if (session != null) {
      Object uuidObj = session.getAttribute("UUID");
      if (uuidObj == null) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getName() != null) {
          AccountEntity account = accountService.findByUsername(authentication.getName());
          if (account != null) {
            session.setAttribute("UUID", account.getUuid());
          }
        }
      }
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
