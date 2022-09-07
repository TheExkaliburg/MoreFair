package de.kaliburg.morefair.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import de.kaliburg.morefair.account.AccountEntity;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

  private final SecureRandom secureRandom = new SecureRandom();
  private final JwtConfig jwtConfig;


  public String generatePassword() {
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
        .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
        .withIssuer(request.getRequestURL().toString())
        .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .sign(getAlgorithm());

    String refreshToken = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
        .withIssuer(request.getRequestURL().toString())
        .sign(getAlgorithm());

    HashMap<String, String> tokens = new HashMap<>();
    tokens.put("access_token", accessToken);
    tokens.put("refresh_token", refreshToken);
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

  public Algorithm getAlgorithm() {
    return Algorithm.HMAC256(jwtConfig.getSecret().getBytes());
  }
}
