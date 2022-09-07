package de.kaliburg.morefair.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws IOException, ServletException {

    if (request.getServletPath().equals("/api/auth/login") || request.getServletPath()
        .equals("/api/auth/register") || request.getServletPath().equals("/api/auth/refresh")) {
      filterChain.doFilter(request, response);
    } else {
      String authorizationHeader = request.getHeader(AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        try {
          String token = authorizationHeader.substring("Bearer ".length());
          Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
          JWTVerifier verifier = JWT.require(algorithm).build();
          DecodedJWT decodedJwt = verifier.verify(token);
          String username = decodedJwt.getSubject();
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