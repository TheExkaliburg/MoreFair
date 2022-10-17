package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountDetailsDto;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.AchievementsEntity;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Log4j2
@RequestMapping("/api/account")
@RestController
@RequiredArgsConstructor
public class AccountController {

  private static final String APP_LOGIN_DESTINATION = "/account/login";
  private static final String APP_RENAME_DESTINATION = "/account/name";
  private static final String QUEUE_LOGIN_DESTINATION = "/account/login";

  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final RoundService roundService;
  private final RankerService rankerService;


  @GetMapping
  public ResponseEntity<?> getAccount(HttpServletRequest request, Authentication authentication,
      HttpServletResponse response) {
    try {
      AccountEntity account = accountService.findByUsername("da539786-83f1-4ff2-92b9-aea2634a27d8");

      if (account.getAchievements() == null) {
        account.setAchievements(new AchievementsEntity(account));
        accountService.save(account);
      }

      RoundEntity currentRound = roundService.getCurrentRound();
      int highestLadder = rankerService.findCurrentRankersOfAccount(account, currentRound).stream()
          .mapToInt(r -> r.getLadder().getNumber()).max().orElse(1);

      return ResponseEntity.ok(new AccountDetailsDto(account, highestLadder));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }


  @PatchMapping("/name")
  public ResponseEntity<?> updateDisplayName(Authentication authentication,
      @RequestParam("displayName") String displayName) {
    try {
      displayName = displayName.trim();
      if (displayName.length() > 32) {
        displayName = displayName.substring(0, 32);
      }

      AccountEntity account = accountService.findByUsername(authentication.getName());

      log.info("[G] RENAME: {} (#{}) -> {}", account.getDisplayName(), account.getId(),
          displayName);

      account.setDisplayName(displayName);
      accountService.save(account);

      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION,
          new Event(EventType.NAME_CHANGE, account.getId(),
              account.getDisplayName()));

      return ResponseEntity.ok(displayName);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

}
