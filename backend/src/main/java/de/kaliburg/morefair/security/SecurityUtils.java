package de.kaliburg.morefair.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.kaliburg.morefair.account.AccountEntity;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

  private static final SecureRandom secureRandom = new SecureRandom();
  private final JwtConfig jwtConfig;


  public static String generatePassword() {
    String passwordCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
    String pwd = RandomStringUtils.random(20, 0, passwordCharacters.length() - 1, false, false,
        passwordCharacters.toCharArray(), secureRandom);
    log.debug("Generated random password: {}", pwd);
    return pwd;
  }

  // generate access and refresh jwt tokens
  public HashMap<String, String> generateTokens(HttpServletRequest request, User user) {
    String accessToken = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
        .withIssuedAt(Instant.now())
        .withIssuer(request.getRequestURL().toString())
        .withClaim("issuedAt", Instant.now().toEpochMilli())
        .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .sign(getAlgorithm());

    String refreshToken = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
        .withIssuedAt(Instant.now())
        .withIssuer(request.getRequestURL().toString())
        .withClaim("issuedAt", Instant.now().toEpochMilli())
        .sign(getAlgorithm());

    HashMap<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);
    return tokens;
  }

  public User convertAccountToUser(AccountEntity account) {
    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(account.getAccessRole().name()));
    return new User(account.getUsername(), account.getPassword(), authorities);
  }

  public HashMap<String, String> generateTokens(HttpServletRequest request,
      AccountEntity account) {
    return generateTokens(request, convertAccountToUser(account));
  }

  public DecodedJWT verifyToken(String token) {
    return JWT.require(getAlgorithm()).build().verify(token);
  }

  public Algorithm getAlgorithm() {
    return Algorithm.HMAC256(jwtConfig.getSecret().getBytes());
  }

  public DecodedJWT getJwtFromRequest(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring("Bearer ".length());
      return verifyToken(token);
    }
    return null;
  }
}
