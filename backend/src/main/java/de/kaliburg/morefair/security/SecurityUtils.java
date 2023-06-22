package de.kaliburg.morefair.security;

import de.kaliburg.morefair.api.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

  private static final SecureRandom secureRandom = new SecureRandom();


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

  public static UUID getUuid(Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetailsWithUuid) {
      return ((UserDetailsWithUuid) principal).getUuid();
    }
    return null;
  }

  public static UUID getUuid(Principal principal) {
    if (principal == null) {
      return null;
    }
    if (principal instanceof Authentication) {
      return getUuid((Authentication) principal);
    }
    return null;
  }
}
