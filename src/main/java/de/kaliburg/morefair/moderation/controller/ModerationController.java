package de.kaliburg.morefair.moderation.controller;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.moderation.data.ModChatData;
import de.kaliburg.morefair.moderation.data.ModerationInfoData;
import de.kaliburg.morefair.persistence.entity.Message;
import de.kaliburg.morefair.service.MessageService;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.UUID;

@Controller
@Log4j2
public class ModerationController {
    private final static String INFO_DESTINATION = "/queue/mod/info";

    private final AccountService accountService;
    private final WSUtils wsUtils;
    private final RankerService ladderService;
    private final MessageService messageService;

    public ModerationController(AccountService accountService, WSUtils wsUtils, RankerService ladderService, MessageService messageService) {
        this.accountService = accountService;
        this.wsUtils = wsUtils;
        this.ladderService = ladderService;
        this.messageService = messageService;
    }

    @GetMapping("/moderation")
    public String getIndex() {
        return "moderation";
    }

    @MessageMapping("/mod/info")
    public void info(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                Integer highestLadder = ladderService.getHighestLadder().getNumber();
                ModerationInfoData infoData = new ModerationInfoData(highestLadder, account.getAccessRole());
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, infoData);
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/chat")
    public void chat(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                ArrayList<Message> messages = messageService.getAllMessages();
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, new ModChatData(messages));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/game")
    public void game(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                Integer highestLadder = ladderService.getHighestLadder().getNumber();
                ModerationInfoData infoData = new ModerationInfoData(highestLadder, account.getAccessRole());
                wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, infoData);
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
