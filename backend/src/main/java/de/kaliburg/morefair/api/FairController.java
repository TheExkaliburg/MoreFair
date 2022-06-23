package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.dto.InfoDTO;
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

  public final static Integer MINIMUM_PEOPLE_FOR_PROMOTE = 1;
  // 250 Millionen
  public final static BigInteger POINTS_FOR_PROMOTE = new BigInteger("100");
  // 500 Thousand
  public final static BigInteger BASE_VINEGAR_NEEDED_TO_THROW = new BigInteger("500000");
  // 2 Thousand
  public final static BigInteger BASE_GRAPES_NEEDED_TO_AUTO_PROMOTE = new BigInteger("5000");
  public final static Integer AUTO_PROMOTE_LADDER = 1;
  public final static Integer MANUAL_PROMOTE_WAIT_TIME = 30;
  public final static Integer BASE_ASSHOLE_LADDER = 1;
  public final static Integer ASSHOLES_FOR_RESET = 1;
  public final static List<String> ASSHOLE_TAGS = new ArrayList<>(
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
  public final static String INFO_DESTINATION = "/queue/info";
  private final AccountService accountService;
  private final WsUtils wsUtils;

  public FairController(AccountService accountService, WsUtils wsUtils) {
    this.accountService = accountService;
    this.wsUtils = wsUtils;
  }

  @GetMapping(value = {"/options", "/help", "/mod", "/changelog"})
  public String forward() {
    return "forward:/";
  }

  @MessageMapping("/info")
  public void login(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.debug("/app/info {}", uuid);

      Integer maxTimesAssholes = accountService.findMaxTimesAsshole();
      InfoDTO info = new InfoDTO(maxTimesAssholes);

      wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, info);
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
