package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.dto.InfoDto;
import de.kaliburg.morefair.game.round.RoundService;
import lombok.extern.slf4j.Slf4j;
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
  public final FairConfig fairConfig;
  private final RoundService roundService;
  private final WsUtils wsUtils;

  public FairController(FairConfig fairConfig, RoundService roundService, WsUtils wsUtils) {
    this.fairConfig = fairConfig;
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
      String uuid = wsMessage.getUuid();
      log.trace("/app/info {}", uuid);

      InfoDto info = new InfoDto(roundService.getCurrentRound(), fairConfig);

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
