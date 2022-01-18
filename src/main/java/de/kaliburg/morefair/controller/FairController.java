package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.InfoDTO;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
public class FairController {
    public final static Integer MINIMUM_PEOPLE_FOR_PROMOTE = 10;
    // 250 Millionen
    public final static BigInteger POINTS_FOR_PROMOTE = new BigInteger("250000000");
    // 2.5 Million
    public final static BigInteger BASE_VINEGAR_NEEDED_TO_THROW = new BigInteger("2500000");
    public final static Integer ASSHOLE_LADDER = 20;
    public final static Integer ASSHOLES_FOR_RESET = 10;
    public final static List<String> ASSHOLE_TAGS = new ArrayList<>(Arrays.asList(
            "",
            "&emsp;(&#x2660;)",     // 01♠
            "&emsp;(&#x2663;)",     // 02♣
            "&emsp;(&#x2665;)",     // 03♥
            "&emsp;(&#x2666;)",     // 04♦
            "&emsp;(&#x2664;)",     // 05♤
            "&emsp;(&#x2667;)",     // 06♧
            "&emsp;(&#x2661;)",     // 07♡
            "&emsp;(&#x2662;)",     // 08♢
            "&emsp;(&#x265F;)",     // 09♟
            "&emsp;(&#x2659;)",     // 10♙
            "&emsp;(&#x265E;)",     // 11♞
            "&emsp;(&#x2658;)",     // 12♘
            "&emsp;(&#x265D;)",     // 13♝
            "&emsp;(&#x2657;)",     // 14♗
            "&emsp;(&#x265C;)",     // 15♜
            "&emsp;(&#x2656;)",     // 16♖
            "&emsp;(&#x265B;)",     // 17♛
            "&emsp;(&#x2655;)",     // 18♕
            "&emsp;(&#x265A;)",     // 19♚
            "&emsp;(&#x2654;)"      // 20♔
    ));
    public final static String INFO_DESTINATION = "/queue/info";
    private final AccountService accountService;
    private final WSUtils wsUtils;


    public FairController(AccountService accountService, WSUtils wsUtils) {
        this.accountService = accountService;
        this.wsUtils = wsUtils;
    }

    @GetMapping("/")
    public String getIndex() {
        return "fair";
    }

    @MessageMapping("/info")
    public void login(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/info {}", uuid);

            InfoDTO info = new InfoDTO();
            info.setAssholeTags(info.getAssholeTags().subList(0, Math.min(accountService.findMaxTimeAsshole() + 2, info.getAssholeTags().size())));
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
