package de.kaliburg.morefair.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

  private final SecurityUtils securityUtils;
  private final AccountService accountService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws IOException, ServletException {

    String servletPath = request.getServletPath();
    // If servlet path is empty get from request url
    if (servletPath.isEmpty()) {
      servletPath = request.getRequestURI();
    }
    if (servletPath.equals("/api/auth/login")
        || servletPath.equals("/api/auth/register")
        || servletPath.equals("/api/auth/register/guest")
        || servletPath.equals("/api/auth/refresh")
        || servletPath.equals("/api/auth/password/forgot")
        || servletPath.equals("/api/auth/password/reset")) {
      filterChain.doFilter(request, response);
    } else {
      String authorizationHeader = request.getHeader(AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        try {
          String token = authorizationHeader.substring("Bearer ".length());
          DecodedJWT decodedJwt = securityUtils.verifyToken(token);
          String username = decodedJwt.getSubject();

          if (Instant.now().isBefore(decodedJwt.getIssuedAtAsInstant())) {
            throw new RuntimeException("Token is not valid yet");
          }
          if (Instant.now().isAfter(decodedJwt.getExpiresAtAsInstant())) {
            throw new Exception("Token is not valid anymore");
          }

          AccountEntity account = accountService.findByUsername(username);
          if (account == null) {
            throw new Exception("User not found");
          }
          if (account.getLastRevokeAsInstant().isAfter(decodedJwt.getIssuedAtAsInstant())) {
            throw new Exception("Token revoked");
          }

          String[] roles = decodedJwt.getClaim("roles").asArray(String.class);
          Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
          Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

          UsernamePasswordAuthenticationToken authenticationToken =
              new UsernamePasswordAuthenticationToken(username, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          filterChain.doFilter(request, response);
        } catch (Exception e) {
          log.error("Failed Authorization: {}", e.getMessage());
          Map<String, String> errors = new HashMap<>();
          errors.put("error", e.getMessage());
          response.setContentType(APPLICATION_JSON_VALUE);
          new ObjectMapper().writeValue(response.getOutputStream(), errors);
        }
      } else {
        filterChain.doFilter(request, response);
      }
    }
  }
}