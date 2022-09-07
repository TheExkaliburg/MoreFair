package de.kaliburg.morefair.api;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AccountService accountService;

  @PostMapping("/signup")
  public void signup(@RequestParam String email, @RequestParam String password) {
    log.debug("signup: email={}, password={}", email, password);
  }

  @GetMapping("/refresh")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    String authorizationHeader = request.getHeader(AUTHORIZATION);
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      try {
        String token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJwt = verifier.verify(token);
        String username = decodedJwt.getSubject();

        AccountEntity account = accountService.findByUsername(username);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(account.getAccessRole().name()));

        String accessToken = JWT.create()
            .withSubject(account.getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .withIssuer(request.getRequestURL().toString())
            .withClaim("roles", authorities)
            .sign(algorithm);

      } catch (Exception e) {
        log.error("Error logging in: {}", e.getMessage());
        response.setHeader("error", e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Map<String, String> error = new HashMap<>();
        error.put("error_message", e.getMessage());
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
        return;
      }
    } else {
      throw new RuntimeException("Refresh token is missing");
    }
  }
}
