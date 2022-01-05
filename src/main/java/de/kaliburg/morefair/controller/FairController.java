package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.InfoDTO;
import de.kaliburg.morefair.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
public class FairController {
    public static final Integer UPDATE_LADDER_STEPS_BEFORE_SYNC = 10;
    public static final Integer UPDATE_CHAT_STEPS_BEFORE_SYNC = 30;
    public final static Integer LADDER_AREA_SIZE = 10;
    public final static Integer PEOPLE_FOR_PROMOTE = 2;
    // 250 Millionen
    public final static BigInteger POINTS_FOR_PROMOTE = new BigInteger("250");
    public final static Integer LADDER_AREA_SIZE_SERVER = 30;
    public final static Integer ASSHOLE_LADDER = 1;
    public final static Integer ASSHOLE_FOR_RESET = 2;
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

    private final AccountService accountService;

    public FairController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String getIndex() {
        return "fair";
    }

    @GetMapping(value = "/fair/info", produces = "application/json")
    public ResponseEntity<InfoDTO> getInfo() {
        InfoDTO info = new InfoDTO();
        // Lets hope this works :)
        info.setAssholeTags(info.getAssholeTags().subList(0, Math.min(accountService.findMaxTimeAsshole() + 2, info.getAssholeTags().size())));
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
