package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.exceptions.ErrorDto;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.mapper.RoundMapper;
import de.kaliburg.morefair.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  public static final String TOPIC_EVENTS_DESTINATION = "/round/events";

  private final RoundService roundService;
  private final AccountService accountService;
  private final RoundMapper roundMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getCurrentRound(Authentication authentication,
      HttpServletRequest request) {
    try {
      if (authentication != null) {
        AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
            .orElse(null);
        if (account != null) {
          Integer ip = SecurityUtils.getIp(request);
          account.setLastLogin(OffsetDateTime.now());
          account.setLastIp(ip);
          accountService.save(account);
        }
      }
      return ResponseEntity.ok(roundMapper.mapToRoundDto(roundService.getCurrentRound()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.internalServerError().body(new ErrorDto(e));
    }
  }
}
