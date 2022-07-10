package de.kaliburg.morefair.api;

import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.dto.InfoDto;
import de.kaliburg.morefair.game.round.RoundService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class FairController {

  public static final String APP_INFO_DESTINATION = "/info";
  public static final String QUEUE_INFO_DESTINATION = APP_INFO_DESTINATION;
  public static final Integer MINIMUM_PEOPLE_FOR_PROMOTE = 1;
  // 100 Millionen
  public static final BigInteger POINTS_FOR_PROMOTE = new BigInteger("100");
  // 500 Thousand
  public static final BigInteger BASE_VINEGAR_NEEDED_TO_THROW = new BigInteger("500");
  // 5 Thousand
  public static final BigInteger BASE_GRAPES_NEEDED_TO_AUTO_PROMOTE = new BigInteger("50");
  public static final Integer AUTO_PROMOTE_LADDER = 1;
  public static final Integer MANUAL_PROMOTE_WAIT_TIME = 10;
  public static final Integer BASE_ASSHOLE_LADDER = 1;
  public static final Integer ASSHOLES_FOR_RESET = 1;
  public static final List<String> ASSHOLE_TAGS = new ArrayList<>(
      Arrays.asList("", "♠",     // 01♠
          "♣",     // 02♣
          "♥",     // 03♥
          "♦",     // 04♦
          "♤",     // 05♤
          "♧",     // 06♧
          "♡",     // 07♡
          "♢",     // 08♢
          "♟",     // 09♟
          "♙",     // 10♙
          "♞",     // 11♞
          "♘",     // 12♘
          "♝",     // 13♝
          "♗",     // 14♗
          "♜",     // 15♜
          "♖",     // 16♖
          "♛",     // 17♛
          "♕",     // 18♕
          "♚",     // 19♚
          "♔",     // 20♔
          "🂠",
          "🂡",
          "🂢",
          "🂣",
          "🂣",
          "🂥",
          "🂦",
          "🂧",
          "🂧",
          "🂩",
          "🂪",
          "🂫",
          "🂬",
          "🂭",
          "🂮"
      ));

  private final RoundService roundService;
  private final WsUtils wsUtils;

  public FairController(RoundService roundService, WsUtils wsUtils) {
    this.roundService = roundService;
    this.wsUtils = wsUtils;
  }

  @GetMapping(value = {"/options", "/help", "/mod", "/changelog"})
  public String forward() {
    return "forward:/";
  }

  @MessageMapping(APP_INFO_DESTINATION)
  public void login(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.trace("/app/info {}", uuid);

      Integer maxTimesAssholes = roundService.getCurrentRound().getHighestAssholeCount();
      InfoDto info = new InfoDto(maxTimesAssholes);

      wsUtils.convertAndSendToUser(sha, QUEUE_INFO_DESTINATION, info);
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INFO_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
