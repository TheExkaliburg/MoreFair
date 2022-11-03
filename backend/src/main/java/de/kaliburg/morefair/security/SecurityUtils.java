package de.kaliburg.morefair.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.kaliburg.morefair.api.utils.HttpUtils;
import java.security.SecureRandom;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

  private static final SecureRandom secureRandom = new SecureRandom();
  private final Secrets secrets;
  private final Argon2PasswordEncoder argon2PasswordEncoder;


  public static String generatePassword() {
    String passwordCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()-_=+[{]}|:',<.>/?";
    String pwd = RandomStringUtils.random(20, 0, passwordCharacters.length() - 1, false, false,
        passwordCharacters.toCharArray(), secureRandom);
    log.debug("Generated random password: {}", pwd);
    return pwd;
  }

  public static Integer getIp(HttpServletRequest request) {
    try {
      return HttpUtils.getIp(request);
    } catch (Exception e) {
      log.error("Could not determine IP", e);
      return null;
    }
  }

  public DecodedJWT verifyToken(String token) {
    return JWT.require(getAlgorithm()).build().verify(token);
  }

  public Algorithm getAlgorithm() {
    return Algorithm.HMAC256(secrets.getRememberMeKey().getBytes());
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
