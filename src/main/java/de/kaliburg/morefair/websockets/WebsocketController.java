package de.kaliburg.morefair.websockets;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {
    private final AccountService accountService;
    private final LadderService ladderService;
    private final RankerService rankerService;


    public WebsocketController(AccountService accountService, LadderService ladderService, RankerService rankerService) {
        this.accountService = accountService;
        this.ladderService = ladderService;
        this.rankerService = rankerService;
    }

    @MessageMapping("/init")
    @SendTo("/updates/ladder")
    public Message ladder(AccountDetailsDTO accountDetailsDTO) throws Exception {
        Account account = accountService.findAccountByUUID(accountDetailsDTO.getUuid());
        return new Message("This is the new Ladder");
    }
}
