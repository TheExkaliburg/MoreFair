package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.exceptions.ErrorDto;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.dto.RoundDto;
import de.kaliburg.morefair.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/api/round")
@RequiredArgsConstructor
public class RoundController {

  public static final String TOPIC_EVENTS_DESTINATION = "/round/event";

  private final RoundService roundService;
  private final FairConfig fairConfig;
  private final AccountService accountService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getCurrentRound(Authentication authentication, HttpServletRequest request) {
    try {
      if(authentication != null) {
        Integer ip = SecurityUtils.getIp(request);
        AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
        if(account != null) {
          account.setLastLogin(OffsetDateTime.now());
          account.setLastIp(ip);
          accountService.save(account);
        }
      }

      return new ResponseEntity<>(new RoundDto(roundService.getCurrentRound(), fairConfig),
          HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body(new ErrorDto(e));
    }
  }
}
