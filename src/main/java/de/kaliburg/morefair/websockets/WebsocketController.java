package de.kaliburg.morefair.websockets;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.messages.WSMessageAnswer;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {
    private final AccountService accountService;
    private final LadderService ladderService;
    private final RankerService rankerService;

    private final SimpMessagingTemplate simpMessagingTemplate;


    public WebsocketController(AccountService accountService, LadderService ladderService, RankerService rankerService, SimpMessagingTemplate simpMessagingTemplate) {
        this.accountService = accountService;
        this.ladderService = ladderService;
        this.rankerService = rankerService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/init")
    @SendTo("/queue/ladder")
    public void ladder(SimpMessageHeaderAccessor sha, AccountDetailsDTO accountDetailsDTO) throws Exception {
        Account account = accountService.findAccountByUUID(accountDetailsDTO.getUuid());
        simpMessagingTemplate.convertAndSendToUser(sha.getUser().getName(), "/queue/ladder", new WSMessageAnswer<>("This is ladder"));
    }
}
