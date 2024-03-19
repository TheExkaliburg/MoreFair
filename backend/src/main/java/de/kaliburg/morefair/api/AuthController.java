package de.kaliburg.morefair.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.utils.HttpUtils;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.mail.services.EmailService;
import de.kaliburg.morefair.security.SecurityUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

  private final Cache<String, UserRegistrationDetails> registrationTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build();

  private final Cache<String, String> passwordResetTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build();

  private final Cache<String, EmailChangeRequest> changeEmailTokens =
      Caffeine.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build();

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
      HttpServletRequest request, @RequestParam(required = false) String uuid) {
    try {
      username = username.toLowerCase();

      if (password.length() < 8) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at least 8 characters long");
      }
      if (password.length() > 64) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at most 64 characters long");
      }
      if (username.length() > 254) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Email must be at most 254 characters long");
      }

      if (!emailRegexPattern.matcher(username).matches()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Invalid email address");
      }

      if (accountService.findByUsername(username) != null) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Email address already in use");
      }

      Integer ip = HttpUtils.getIp(request);

      if (!requestThrottler.canCreateAccount(ip)) {
        return HttpUtils.buildErrorMessage(HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests");
      }

      String confirmToken = UUID.randomUUID().toString();
      registrationTokens.put(confirmToken,
          new UserRegistrationDetails(username, password, uuid, false));
      emailService.sendRegistrationMail(username, confirmToken);

      Map<String, String> response = new HashMap<>();
      response.put("message", "Please look into your inbox for a confirmation link");
      URI uri = HttpUtils.createCreatedUri("/api/auth/register");
      return ResponseEntity.created(uri).body(response);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }


  }

  // API endpoint for changing password in combination with the old password
  @PostMapping(value = "/password/change", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
      @RequestParam String newPassword, HttpServletRequest request,
      HttpServletResponse response, Authentication authentication) {
    try {
      if (newPassword.length() < 8) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at least 8 characters long.");
      }
      if (newPassword.length() > 64) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at most 64 characters long.");
      }

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElseThrow();

      if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Wrong password");
      }

      account.setPassword(passwordEncoder.encode(newPassword));
      accountService.save(account);
      SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
      handler.logout(request, response, null);

      return ResponseEntity.ok("Password changed, please log back in with your new password.");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  // API for Creating, saving and sending a new token via mail for resetting the password
  @PostMapping(value = "/password/forgot", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> forgotPassword(@RequestParam("username") String username) {
    try {
      if (!emailRegexPattern.matcher(username).matches()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Invalid email address");
      }

      username = username.toLowerCase();
      URI uri = HttpUtils.createCreatedUri("/api/auth/password/forgot");
      AccountEntity account = accountService.findByUsername(username).orElse(null);
      if (account == null || account.isGuest()) {
        return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
      }

      String confirmToken = UUID.randomUUID().toString();
      passwordResetTokens.put(confirmToken, username);
      emailService.sendPasswordResetMail(username, confirmToken);

      return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");

    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  // API endpoint for changing password in combination with a passwordResetToken
  @PostMapping(value = "/password/reset", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> resetPassword(@RequestParam("token") String resetToken,
      @RequestParam("password") String newPassword, HttpServletRequest request,
      HttpServletResponse response) {

    try {
      if (newPassword.length() < 8) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at least 8 characters long");
      }
      if (newPassword.length() > 64) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Password must be at most 64 characters long");
      }

      String username = passwordResetTokens.getIfPresent(resetToken);
      if (username == null) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Invalid token");
      }

      HttpSession session = request.getSession(false);
      if (session != null) {
        SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
        handler.logout(request, response, null);
      }

      AccountEntity account = accountService.findByUsername(username).orElseThrow();
      account.setPassword(passwordEncoder.encode(newPassword));
      accountService.save(account);

      Map<String, String> result = new HashMap<>();
      result.put("message", "Password changed");
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @GetMapping(value = "/register/confirm", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<?> confirmRegistration(@RequestParam String token,
      HttpServletRequest request, HttpServletResponse response, HttpSession session) {
    try {
      UserRegistrationDetails details = registrationTokens.getIfPresent(token);
      if (details == null) {
        return ResponseEntity.notFound().build();
      }
      if (details.isUsed()) {
        return ResponseEntity.notFound().build();
      }

      Integer ip = HttpUtils.getIp(request);
      String uuid = details.getUuid();
      String username = details.getUsername();
      String password = details.getPassword();

      if (uuid != null && !uuid.isEmpty()) {
        AccountEntity account = accountService.findByUsername(uuid).orElse(null);
        if (account != null && account.isGuest()) {
          account.setUsername(username);
          account.setPassword(passwordEncoder.encode(password));
          account.setLastLogin(OffsetDateTime.now());
          account.setLastIp(ip);
          account.setGuest(false);
          accountService.save(account);
        } else if (account == null) {
          account = accountService.create(username, password, ip, false).orElseThrow();
          account.setLastIp(ip);
          account.setLastLogin(OffsetDateTime.now());
          accountService.save(account);
        }
      } else {
        AccountEntity account = accountService.create(username, password, ip, false).orElseThrow();
        account.setLastIp(ip);
        account.setLastLogin(OffsetDateTime.now());
        accountService.save(account);
      }

      registrationTokens.put(token,
          new UserRegistrationDetails(username, SecurityUtils.generatePassword(), uuid, true));

      // delete _uuid cookie
      Cookie emptyCookie = new Cookie("_uuid", null);
      emptyCookie.setPath("/");
      emptyCookie.setMaxAge(0);
      response.addCookie(emptyCookie);

      SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
      handler.logout(request, response, null);

      URI uri = HttpUtils.createCreatedUri("/api/auth/register/confirm");
      return ResponseEntity.created(uri)
          .body("Registration successful; Please log into your account");

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

  }

  @PostMapping(value = "/register/guest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> registerGuest(HttpServletRequest request) {
    try {
      Integer ip = HttpUtils.getIp(request);

      if (!requestThrottler.canCreateAccount(ip)) {
        return HttpUtils.buildErrorMessage(HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests");
      }

      UUID uuid = UUID.randomUUID();
      AccountEntity account = accountService.create(uuid.toString(), uuid.toString(), ip, true)
          .orElseThrow();

      URI uri = HttpUtils.createCreatedUri("/api/auth/signup/guest");
      return ResponseEntity.created(uri).body(account.getUsername());

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

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
      @RequestParam("email") String newMail, HttpSession session) {
    try {
      newMail = newMail.toLowerCase();
      if (newMail.length() > 254) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "Email must be at most 254 characters long");
      }
      if (!emailRegexPattern.matcher(newMail).matches()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Invalid email address");
      }

      if (accountService.findByUsername(newMail) != null) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Email address already in use");
      }

      URI uri = HttpUtils.createCreatedUri("/api/account/email");
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElseThrow();

      if (account.isGuest()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST,
            "You cannot change the email of a guest account");
      }

      String confirmToken = UUID.randomUUID().toString();
      changeEmailTokens.put(confirmToken, new EmailChangeRequest(account.getUuid(), newMail));
      emailService.sendChangeEmailMail(newMail, confirmToken);

      return ResponseEntity.created(uri).body("Please look into your inbox for the reset token");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @PostMapping("/email")
  public ResponseEntity<?> confirmUpdatedEmail(@RequestParam("token") String token,
      HttpSession session, HttpServletRequest request, HttpServletResponse response) {
    try {
      token = token.trim();

      EmailChangeRequest details = changeEmailTokens.getIfPresent(token);
      if (details == null) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Invalid or expired token");
      }

      var optionalAccount = accountService.findByUuid(details.getUuid());
      if (optionalAccount.isEmpty()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Account not found");
      }
      var account = optionalAccount.get();

      account.setUsername(details.getEmail());
      accountService.save(account);

      SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
      handler.logout(request, response, null);

      Map<String, Object> result = new HashMap<>();
      result.put("message", "Your email address has been updated");
      result.put("email", details.getEmail());

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
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

