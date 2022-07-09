package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.api.websockets.messages.WsObservedMessage;
import de.kaliburg.morefair.data.ModServerMessageData;
import de.kaliburg.morefair.dto.LadderDto;
import de.kaliburg.morefair.dto.LadderResultsDto;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.GameService;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.RoundUtils;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class GameController {

  public static final String APP_INIT_DESTINATION = "/game/init/{number}";
  public static final String APP_BIAS_DESTINATION = "/game/bias";
  public static final String APP_MULTI_DESTINATION = "/game/multi";
  public static final String APP_VINEGAR_DESTINATION = "/game/vinegar";
  public static final String APP_PROMOTE_DESTINATION = "/game/promote";
  public static final String APP_AUTOPROMOTE_DESTINATION = "/game/autopromote";
  public static final String TOPIC_TICK_DESTINATION = "/game/tick";
  public static final String TOPIC_EVENTS_DESTINATION = "/game/events/{number}";
  public static final String TOPIC_GLOBAL_EVENTS_DESTINATION = "/game/events";
  public static final String QUEUE_INIT_DESTINATION = "/game/init";

  public static final String PRIVATE_EVENTS_DESTINATION = "/game/events";

  private final RankerService rankerService;
  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final RoundService roundService;
  private final LadderService ladderService;
  private final GameService gameService;
  private final RoundUtils roundUtils;

  public GameController(RankerService rankerService, AccountService accountService,
      WsUtils wsUtils, RoundService roundService, LadderService ladderService,
      GameService gameService, RoundUtils roundUtils) {
    this.rankerService = rankerService;
    this.accountService = accountService;
    this.wsUtils = wsUtils;
    this.roundService = roundService;
    this.ladderService = ladderService;
    this.gameService = gameService;
    this.roundUtils = roundUtils;
  }

  @GetMapping(value = "/lastRound", produces = "application/json")
  public ResponseEntity<LadderResultsDto> getStatistics() {
    try {
      if (roundService.getLastRoundResults() == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      // TODO: Logic in Service
      return new ResponseEntity<>(roundService.getLastRoundResults(), HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping(APP_INIT_DESTINATION)
  public void initLadder(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("number") Integer number) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.debug("/app/game/init/{} from {}", number, uuid);
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }

      RankerEntity ranker = rankerService.findHighestActiveRankerOfAccount(account);

      if (ranker == null) {
        ranker = roundService.createNewRanker(account);
      }

      if (account.isMod()
          || number.equals(roundUtils.getAssholeLadderNumber(roundService.getCurrentRound()))
          || number <= ranker.getLadder().getNumber()) {
        LadderDto l = new LadderDto(ladderService.find(number), account);
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, l);
      } else {
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION,
            "Can't get permission to view the ladder.",
            HttpStatus.FORBIDDEN);
      }

    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_BIAS_DESTINATION)
  public void buyBias(SimpMessageHeaderAccessor sha, WsObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerOfAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] BIAS: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event(EventType.BUY_BIAS, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MULTI_DESTINATION)
  public void buyMulti(SimpMessageHeaderAccessor sha, WsObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerOfAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] MULTI: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event(EventType.BUY_MULTI, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_VINEGAR_DESTINATION)
  public void throwVinegar(SimpMessageHeaderAccessor sha, WsObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerOfAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] VINEGAR: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event(EventType.THROW_VINEGAR, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_PROMOTE_DESTINATION)
  public void promote(SimpMessageHeaderAccessor sha, WsObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerOfAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] PROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event(EventType.PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_AUTOPROMOTE_DESTINATION)
  public void buyAutoPromote(SimpMessageHeaderAccessor sha, WsObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerOfAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] AUTOPROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToTopic(ModerationController.TOPIC_EVENTS_DESTINATION + num, data);
      ladderService.addEvent(num, new Event(EventType.BUY_AUTO_PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
