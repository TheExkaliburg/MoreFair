package de.kaliburg.morefair.api;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.HttpUtils;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.serivces.EmailService;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  private final SecurityUtils securityUtils;
  private final EmailService emailService;

  private final Pattern emailRegexPattern = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

  private final LoadingCache<String, UserRegistrationDetails> userThatRequestedConfirmation = Caffeine.newBuilder()
      .expireAfterWrite(1, TimeUnit.HOURS)
      .build(uuid -> null);

  // TODO: changePassword
  // TODO: forgotPassword + sendToken via Mail
  // TODO: resetPassword with the previously sent token
  // TODO: revokeJwtTokens for a specific user
  // TODO: config for server-paths to put into mails

  @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> signup(@RequestParam String username, @RequestParam String password,
      HttpServletRequest request, @RequestParam(required = false) String uuid)
      throws Exception {
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

    String confirmToken = UUID.randomUUID().toString();
    userThatRequestedConfirmation.put(confirmToken,
        new UserRegistrationDetails(username, password, uuid, false));
    emailService.sendRegistrationMail(username, confirmToken);

    URI uri = HttpUtils.createCreatedUri("/api/auth/register");
    return ResponseEntity.created(uri).body("Please look into your inbox for a confirmation link");
  }

  @GetMapping(value = "/register/confirm")
  public ResponseEntity<?> signup(@RequestParam String token, HttpServletRequest request)
      throws Exception {
    UserRegistrationDetails details = userThatRequestedConfirmation.get(token);
    if (details == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Invalid or expired confirmation token");
    }
    if (details.isUsed()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Confirmation token already used");
    }

    Integer ip = HttpUtils.getIp(request);
    String uuid = details.getUuid();
    String username = details.getUsername();
    String password = details.getPassword();

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
      account.setLastIp(ip);
      account.setLastLogin(OffsetDateTime.now());
      accountService.save(account);
    }

    userThatRequestedConfirmation.put(token,
        new UserRegistrationDetails(username, securityUtils.generatePassword(), uuid, true));

    URI uri = HttpUtils.createCreatedUri("/api/auth/register/confirm");
    return ResponseEntity.created(uri)
        .body("Registration successful; Please log into your account");
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

  @GetMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> refreshToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(AUTHORIZATION);
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      try {
        String token = authorizationHeader.substring("Bearer ".length());
        DecodedJWT decodedJwt = securityUtils.verifyToken(token);
        String username = decodedJwt.getSubject();
        Instant instant = decodedJwt.getIssuedAt().toInstant();

        AccountEntity account = accountService.findByUsername(username);
        if (account == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (account.getLastRevoke().toInstant().isAfter(instant)) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token revoked");
        }

        HashMap<String, String> tokens = securityUtils.generateTokens(request, account);

        return ResponseEntity.created(HttpUtils.createCreatedUri("/api/auth/refresh"))
            .body(tokens);

      } catch (Exception e) {
        log.error("Error refreshing jwt-tokens: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
      }
    } else {
      throw new RuntimeException("Refresh token is missing");
    }
  }
}

@Data
@RequiredArgsConstructor
@AllArgsConstructor
class UserRegistrationDetails {

  private String username = "";
  private String password = "";
  private String uuid = "";
  private boolean used = true;
}