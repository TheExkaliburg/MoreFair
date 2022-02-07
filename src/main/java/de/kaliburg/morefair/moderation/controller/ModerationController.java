package de.kaliburg.morefair.moderation.controller;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.chat.Message;
import de.kaliburg.morefair.chat.MessageService;
import de.kaliburg.morefair.ladder.RankerService;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.moderation.data.ModChatData;
import de.kaliburg.morefair.moderation.data.ModerationInfoData;
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
    public final static String CHAT_UPDATE_DESTINATION = "/topic/mod/chat";
    public final static String GAME_UPDATE_DESTINATION = "/topic/mod/game";
    public final static String GLOBAL_UPDATE_DESTINATION = "/topic/mod/global";
    private final static String INFO_DESTINATION = "/queue/mod/info";
    private final static String CHAT_DESTINATION = "/queue/mod/chat";
    private final static String GAME_DESTINATION = "/queue/mod/game";
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
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                ArrayList<Message> messages = messageService.getAllMessages();
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, new ModChatData(messages));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/ban")
    public void ban(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                accountService.ban
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
