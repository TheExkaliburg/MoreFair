package de.kaliburg.morefair.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.HttpUtils;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.serivces.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

  private final RequestThrottler requestThrottler;
  private final Pattern emailRegexPattern = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

  private final LoadingCache<String, UserRegistrationDetails> registrationTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build(uuid -> null);

  private final LoadingCache<String, String> passwordResetTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build(uuid -> "");

  private final LoadingCache<String, String> changeEmailTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build(key -> null);

  // TODO: _uuid cookie automatisch setzen (registerGuest), lesen (upgradeAccount) und löschen
  //  (upgradeAccount)
  // TODO: auto reroute to /game on successful login
  // TODO: auto reroute to / if not authenticated

  @GetMapping
  public ResponseEntity<?> getAuthenticationStatus(Authentication authentication) {
    return ResponseEntity.ok(authentication != null && authentication.isAuthenticated());
  }

  @PostMapping(value = "/login")
  public ResponseEntity<?> login() {
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password,
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

    Integer ip = HttpUtils.getIp(request);

    if (!requestThrottler.canCreateAccount(ip)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body("Too many requests");
    }

    String confirmToken = UUID.randomUUID().toString();
    registrationTokens.put(confirmToken,
        new UserRegistrationDetails(username, password, uuid, false));
    emailService.sendRegistrationMail(username, confirmToken);

    URI uri = HttpUtils.createCreatedUri("/api/auth/register");
    return ResponseEntity.created(uri).body("Please look into your inbox for a confirmation link");
  }

  // API endpoint for changing password in combination with the old password
  @PostMapping(value = "/password/change", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
      @RequestParam String newPassword, HttpSession session, Authentication authentication) {

    if (newPassword.length() < 8) {
      return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
    }
    if (newPassword.length() > 64) {
      return ResponseEntity.badRequest().body("Password must be at most 64 characters long");
    }

    UUID uuid = SecurityUtils.getUuid(authentication);

    AccountEntity account = accountService.find(uuid);
    if (account == null) {
      return ResponseEntity.badRequest().body("Account not found");
    }

    if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
      return ResponseEntity.badRequest().body("Wrong password");
    }

    account.setPassword(passwordEncoder.encode(newPassword));
    accountService.save(account);
    session.invalidate();

    return ResponseEntity.ok("Password changed, please log back in with your new password.");
  }

  // API for Creating, saving and sending a new token via mail for resetting the password
  @PostMapping(value = "/password/forgot", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> forgotPassword(@RequestParam String username, HttpServletRequest request)
      throws Exception {
    URI uri = HttpUtils.createCreatedUri("/api/auth/password/forgot");
    AccountEntity account = accountService.findByUsername(username);
    if (account == null || account.isGuest()) {
      return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
    }

    String confirmToken = UUID.randomUUID().toString();
    passwordResetTokens.put(confirmToken, username);
    emailService.sendPasswordResetMail(username, confirmToken);

    return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
  }

  // API endpoint for changing password in combination with a passwordResetToken
  @PostMapping(value = "/password/reset", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> resetPassword(@RequestParam String resetToken,
      @RequestParam String newPassword, HttpServletRequest request) throws Exception {

    if (newPassword.length() < 8) {
      return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
    }
    if (newPassword.length() > 64) {
      return ResponseEntity.badRequest().body("Password must be at most 64 characters long");
    }

    String username = passwordResetTokens.getIfPresent(resetToken);
    if (username == null) {
      return ResponseEntity.badRequest().body("Invalid token");
    }

    AccountEntity account = accountService.findByUsername(username);
    if (account == null) {
      return ResponseEntity.internalServerError().body("Account not found");
    }

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    account.setPassword(passwordEncoder.encode(newPassword));
    accountService.save(account);

    return ResponseEntity.ok("Password changed");
  }


  @GetMapping(value = "/register/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> confirmRegistration(@RequestParam String token,
      HttpServletRequest request)
      throws Exception {
    UserRegistrationDetails details = registrationTokens.getIfPresent(token);
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

    registrationTokens.put(token,
        new UserRegistrationDetails(username, SecurityUtils.generatePassword(), uuid, true));

    URI uri = HttpUtils.createCreatedUri("/api/auth/register/confirm");
    return ResponseEntity.created(uri)
        .body("Registration successful; Please log into your account");
  }

  @PostMapping(value = "/register/guest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> registerGuest(HttpServletRequest request)
      throws Exception {
    Integer ip = HttpUtils.getIp(request);

    if (!requestThrottler.canCreateAccount(ip)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body("Too many requests");
    }

    UUID uuid = UUID.randomUUID();
    AccountEntity account = accountService.create(uuid.toString(), uuid.toString(), ip, true);

    URI uri = HttpUtils.createCreatedUri("/api/auth/signup/guest");
    return ResponseEntity.created(uri).body(account.getUsername());
  }

  /**
   * This PATCH endpoint is used to request an update to the email-address of an account.
   *
   * @param authentication The current authentication of the session.
   * @param newMail        the new email address.
   * @return
   */
  @PatchMapping("/email")
  public ResponseEntity<?> requestUpdatedEmail(Authentication authentication,
      @RequestParam("email") String newMail) {
    try {
      if (newMail.length() > 254) {
        return ResponseEntity.badRequest().body("Email must be at most 254 characters long");
      }
      if (!emailRegexPattern.matcher(newMail).matches()) {
        return ResponseEntity.badRequest().body("Invalid email address");
      }

      if (accountService.findByUsername(newMail) != null) {
        return ResponseEntity.badRequest().body("Email address already in use");
      }

      URI uri = HttpUtils.createCreatedUri("/api/account/email");
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));

      if (account == null || account.isGuest()) {
        return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
      }

      String confirmToken = UUID.randomUUID().toString();
      changeEmailTokens.put(confirmToken, newMail);
      emailService.sendChangeEmailMail(newMail, confirmToken);

      return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @PostMapping("/email")
  public ResponseEntity<?> confirmUpdatedEmail(Authentication authentication,
      @RequestParam("token") String token) {
    token = token.trim();

    String newEmail = changeEmailTokens.getIfPresent(token);
    if (newEmail == null) {
      return ResponseEntity.badRequest().body("Invalid token");
    }

    AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
    if (account == null) {
      return ResponseEntity.badRequest().body("Account not found");
    }

    account.setUsername(newEmail);
    return ResponseEntity.ok(newEmail);
  }

  @Data
  @RequiredArgsConstructor
  @AllArgsConstructor
  public static class UserRegistrationDetails {

    private String username = "";
    private String password = "";
    private String uuid = "";
    private boolean used = true;
  }

  @Data
  @RequiredArgsConstructor
  @AllArgsConstructor
  public static class EmailChangeRequest {

    private UUID uuid;
    private String email;
  }
}

