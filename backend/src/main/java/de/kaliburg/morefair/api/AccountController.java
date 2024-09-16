package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.model.dto.AccountSettingsDto;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.account.services.AccountSettingsService;
import de.kaliburg.morefair.account.services.mapper.AccountMapper;
import de.kaliburg.morefair.account.services.mapper.AccountSettingsMapper;
import de.kaliburg.morefair.api.utils.HttpUtils;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.AccountEventTypes;
import de.kaliburg.morefair.game.ladder.services.LadderTickService;
import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;
import de.kaliburg.morefair.moderation.events.services.NameChangeService;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.statistics.services.StatisticsService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Slf4j
@RequestMapping("/api/account")
@RestController
@RequiredArgsConstructor
public class AccountController {

  public static final String TOPIC_EVENTS_DESTINATION = "/account/events";
  public static final String PRIVATE_EVENTS_DESTINATION = "/account/events";

  private final AccountService accountService;
  private final AccountSettingsService accountSettingsService;
  private final WsUtils wsUtils;
  private final AccountMapper accountMapper;
  private final AccountSettingsMapper accountSettingsMapper;
  private final StatisticsService statisticsService;
  private final LadderTickService ladderTickService;
  private final NameChangeService nameChangeService;

  /**
   * Returns the AccountDetails of an account.
   *
   * @param authentication The Authentication
   * @return A HTTP-Response.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getAccount(Authentication authentication) {
    try (var ignored = ladderTickService.getSemaphore().enter()) {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElseThrow();

      statisticsService.recordLogin(account);

      return ResponseEntity.ok(accountMapper.mapToAccountDetailsDto(account));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }


  /**
   * Updates the Display Name of an Account.
   *
   * @param authentication The Authentication
   * @param displayName    The new display-name
   * @return A HTTP-Response.
   */
  @PatchMapping("/name")
  public ResponseEntity<?> updateDisplayName(Authentication authentication,
      @RequestParam("displayName") String displayName) {
    try {
      while (displayName.contains("[BANNED]") || displayName.contains("[MUTED]")) {
        displayName = displayName.replace("[BANNED]", "");
        displayName = displayName.replace("[MUTED]", "");
      }

      displayName = displayName.trim();
      if (displayName.length() > 32) {
        displayName = displayName.substring(0, 32);
      }

      if (displayName.isBlank()) {
        return HttpUtils.buildErrorMessage(HttpStatus.BAD_REQUEST, "Display name cannot be blank");
      }

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElseThrow();

      if (displayName.equals(account.getDisplayName())) {
        Map<String, String> result = new HashMap<>();
        result.put("displayName", displayName);
        return ResponseEntity.ok(result);
      }

      log.info("[G] RENAME: {} (#{}) -> {}", account.getDisplayName(), account.getId(),
          displayName);

      NameChangeEntity nameChange = nameChangeService.updateDisplayName(account.getId(),
          displayName);

      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION,
          new Event<>(AccountEventTypes.NAME_CHANGE, account.getId(),
              nameChange.getDisplayName()));

      Map<String, String> result = new HashMap<>();
      result.put("displayName", nameChange.getDisplayName());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @PatchMapping("/settings")
  public ResponseEntity<?> updateAccountSettings(Authentication authentication,
      @RequestBody AccountSettingsDto settingsDto) {
    try {
      if (settingsDto == null) {
        return ResponseEntity.badRequest().build();
      }

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElseThrow();

      log.info("[G] SETTINGS: {} (#{}) -> {}", account.getDisplayName(), account.getId(),
          settingsDto);

      var settingsEntity = accountSettingsService.findOrCreateByAccount(account.getId());
      settingsEntity.setVinegarSplit(settingsDto.getVinegarSplit());
      settingsEntity = accountSettingsService.save(settingsEntity);

      return ResponseEntity.ok(accountSettingsMapper.mapToAccountSettingsDto(settingsEntity));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }
}
