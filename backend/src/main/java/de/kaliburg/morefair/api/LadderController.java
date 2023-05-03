package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.data.ModServerMessageData;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventTypes;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.RoundUtils;
import de.kaliburg.morefair.game.round.dto.LadderDto;
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

  public static final String TOPIC_EVENTS_DESTINATION = "/ladder/event/{number}";
  public static final String PRIVATE_EVENTS_DESTINATION = "/ladder/event";
  private static final String APP_BIAS_DESTINATION = "/ladder/bias";
  private static final String APP_MULTI_DESTINATION = "/ladder/multi";
  private static final String APP_VINEGAR_DESTINATION = "/ladder/vinegar";
  private static final String APP_PROMOTE_DESTINATION = "/ladder/promote";
  private static final String APP_AUTOPROMOTE_DESTINATION = "/ladder/autoPromote";
  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final RoundService roundService;
  private final LadderService ladderService;
  private final RoundUtils roundUtils;
  private final FairConfig config;


  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> initLadder(@RequestParam(value = "number") Integer number,
      Authentication authentication) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      log.trace("/app/game/init/{} from {}#{}", number, account.getDisplayName(), account.getId());
      RankerEntity ranker = ladderService.findFirstActiveRankerOfAccountThisRound(account);
      if (ranker == null) {
        ranker = roundService.createNewRanker(account);
      }

      if (account.isMod()
          || number.equals(roundUtils.getAssholeLadderNumber(roundService.getCurrentRound()))
          || number <= ranker.getLadder().getNumber()) {
        return ResponseEntity.ok(l);
        LadderDto l = new LadderDto(ladderService.findInCache(number), account, config);
      } else {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping(APP_BIAS_DESTINATION)
  public void buyBias(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = ladderService.findFirstActiveRankerOfAccountThisRound(account).getLadder()
          .getNumber();
      log.info("[L{}] BIAS: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event<>(LadderEventTypes.BUY_BIAS, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MULTI_DESTINATION)
  public void buyMulti(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return;
      }
      RoundEntity currentRound = roundService.getCurrentRound();
      Integer num = ladderService.findFirstActiveRankerOfAccountThisRound(account).getLadder()
          .getNumber();
      log.info("[L{}] MULTI: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getContent());
      ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event<>(LadderEventTypes.BUY_MULTI, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_VINEGAR_DESTINATION)
  public void throwVinegar(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return;
      }
      RoundEntity currentRound = roundService.getCurrentRound();
      Integer num = ladderService.findFirstActiveRankerOfAccountThisRound(account).getLadder()
          .getNumber();
      log.info("[L{}] VINEGAR: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event<>(LadderEventTypes.THROW_VINEGAR, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_PROMOTE_DESTINATION)
  public void promote(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return;
      }
      RoundEntity currentRound = roundService.getCurrentRound();
      Integer num = ladderService.findFirstActiveRankerOfAccountThisRound(account).getLadder()
          .getNumber();
      log.info("[L{}] PROMOTE: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event<>(LadderEventTypes.PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_AUTOPROMOTE_DESTINATION)
  public void buyAutoPromote(SimpMessageHeaderAccessor sha, Authentication authentication,
      WsMessage wsMessage) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return;
      }
      RoundEntity currentRound = roundService.getCurrentRound();
      Integer num = ladderService.findFirstActiveRankerOfAccountThisRound(account).getLadder()
          .getNumber();
      log.info("[L{}] AUTOPROMOTE: {} (#{}) {}", num, account.getDisplayName(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event<>(LadderEventTypes.BUY_AUTO_PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
