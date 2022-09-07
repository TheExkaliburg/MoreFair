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
import de.kaliburg.morefair.api.utils.HttpUtils;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;

  private final Pattern emailRegexPattern = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

  @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> signup(@RequestParam String username, @RequestParam String password,
      HttpServletRequest request, @RequestParam(required = false) String uuid)
      throws Exception {
    Integer ip = HttpUtils.getIp(request);

    if (password.length() < 8) {
      return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
    }
    if (password.length() > 64) {
      return ResponseEntity.badRequest().body("Password must be at most 64 characters long");
    }
    if (username.length() > 254) {
      return ResponseEntity.badRequest().body("Email must be at most 254 characters long");
    }

    if (!emailRegexPattern.matcher(username).matches()) {
      return ResponseEntity.badRequest().body("Invalid email address");
    }

    if (accountService.findByUsername(username) != null) {
      return ResponseEntity.badRequest().body("Email address already in use");
    }

    // TODO: Send email to make sure that the email is accurate

    if (uuid != null && !uuid.isEmpty()) {
      AccountEntity account = accountService.findByUsername(uuid);
      if (account != null && account.isGuest()) {
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setLastLogin(OffsetDateTime.now());
        account.setLastIp(ip);
        account.setGuest(false);
        accountService.save(account);
      }
    } else {
      AccountEntity account = accountService.create(username, password, ip, false);
      account.setLastLogin(OffsetDateTime.now());
      account.setLastIp(ip);
    }

    URI uri = HttpUtils.createCreatedUri("/api/auth/signup");
    return ResponseEntity.created(uri).build();
  }

  @PostMapping("/register/guest")
  public ResponseEntity<?> guestSignup(HttpServletRequest request)
      throws Exception {
    Integer ip = HttpUtils.getIp(request);
    UUID uuid = UUID.randomUUID();
    AccountEntity account = accountService.create(uuid.toString(), uuid.toString(), ip, true);

    URI uri = HttpUtils.createCreatedUri("/api/auth/signup/guest");
    return ResponseEntity.created(uri).body(account.getUsername());
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
