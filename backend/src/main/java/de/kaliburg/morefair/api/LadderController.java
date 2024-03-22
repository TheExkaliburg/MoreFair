package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.data.ModServerMessageData;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderEventService;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ladder.services.mapper.LadderMapper;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import de.kaliburg.morefair.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
@RequestMapping("/api/ladder")
@RequiredArgsConstructor
public class LadderController {

  public static final String TOPIC_EVENTS_DESTINATION = "/ladder/events/{number}";
  public static final String PRIVATE_EVENTS_DESTINATION = "/ladder/events";
  private static final String APP_BIAS_DESTINATION = "/ladder/bias";
  private static final String APP_MULTI_DESTINATION = "/ladder/multi";
  private static final String APP_VINEGAR_DESTINATION = "/ladder/vinegar";
  private static final String APP_PROMOTE_DESTINATION = "/ladder/promote";
  private static final String APP_AUTOPROMOTE_DESTINATION = "/ladder/autoPromote";
  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final RoundService roundService;
  private final LadderService ladderService;
  private final LadderEventService ladderEventService;
  private final RoundUtilsService roundUtilsService;
  private final LadderMapper ladderMapper;
  private final RankerService rankerService;


  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> initLadder(@RequestParam(value = "number") Integer number,
      Authentication authentication) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      log.trace("/app/game/init/{} from {}#{}", number, account.getDisplayName(), account.getId());

      RoundEntity currentRound = roundService.getCurrentRound();
      var highestLadder = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow());

      if (highestLadder.isEmpty()) {
        log.info("Creating new ranker for {}#{}", account.getDisplayName(), account.getId());
        rankerService.enterNewRanker(account);
      }

      int ladderNumber = highestLadder.map(LadderEntity::getNumber).orElse(1);
      if (!account.isMod()
          && !number.equals(roundUtilsService.getAssholeLadderNumber(currentRound))
          && number > ladderNumber) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      LadderEntity result = ladderService.findCurrentLadderWithNumber(number)
          .orElseThrow();
      return ResponseEntity.ok(
          ladderMapper.mapToLadderDto(result, account)
      );
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping(APP_BIAS_DESTINATION)
  public void buyBias(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return;
      }

      Integer num = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
          .map(LadderEntity::getNumber)
          .orElseThrow();

      log.info("[L{}] BIAS: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_LOG_EVENTS_DESTINATION + num, data);
      ladderEventService.addEvent(num, new Event<>(LadderEventType.BUY_BIAS, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_MULTI_DESTINATION)
  public void buyMulti(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return;
      }

      Integer num = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
          .map(LadderEntity::getNumber)
          .orElseThrow();

      log.info("[L{}] MULTI: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());

      ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_LOG_EVENTS_DESTINATION + num, data);

      ladderEventService.addEvent(num,
          new Event<>(LadderEventType.BUY_MULTI, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_VINEGAR_DESTINATION)
  public void throwVinegar(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return;
      }

      Integer num = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
          .map(LadderEntity::getNumber)
          .orElseThrow();

      log.info("[L{}] VINEGAR: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());

      ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_LOG_EVENTS_DESTINATION + num, data);

      ladderEventService.addEvent(num,
          new Event<>(LadderEventType.THROW_VINEGAR, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_PROMOTE_DESTINATION)
  public void promote(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return;
      }

      Integer num = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
          .map(LadderEntity::getNumber)
          .orElseThrow();

      log.info("[L{}] PROMOTE: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());

      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_LOG_EVENTS_DESTINATION + num, data);

      ladderEventService.addEvent(num, new Event<>(LadderEventType.PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_AUTOPROMOTE_DESTINATION)
  public void buyAutoPromote(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || account.isBanned()) {
        return;
      }

      Integer num = rankerService.findHighestActiveRankerOfAccount(account)
          .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
          .map(LadderEntity::getNumber)
          .orElse(1);

      log.info("[L{}] AUTOPROMOTE: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());

      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_LOG_EVENTS_DESTINATION + num, data);

      ladderEventService.addEvent(num,
          new Event<>(LadderEventType.BUY_AUTO_PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
