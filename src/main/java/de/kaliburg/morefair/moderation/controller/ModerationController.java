package de.kaliburg.morefair.moderation.controller;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.chat.Message;
import de.kaliburg.morefair.chat.MessageService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.ladder.RankerService;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.moderation.data.ModChatData;
import de.kaliburg.morefair.moderation.data.ModerationInfoData;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

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

    @MessageMapping("/mod/ban/{id}")
    public void ban(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                log.info("{} is banning the account with id {}", account.getUsername(), id);
                accountService.addModEvent(new Event(EventType.BAN, id));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/mute/{id}")
    public void mute(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                log.info("{} is muting the account with id {}", account.getUsername(), id);
                accountService.addModEvent(new Event(EventType.MUTE, id));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/free/{id}")
    public void free(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
            } else {
                log.info("{} is freeing the account with id {}", account.getUsername(), id);
                accountService.addModEvent(new Event(EventType.FREE, id));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/name/{id}")
    public void changeName(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            String username = wsMessage.getContent();
            username = username.trim();
            if (username.length() > 64) username = username.substring(0, 64);
            username = StringEscapeUtils.escapeJava(HtmlUtils.htmlEscape(username));

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
                return;
            } else {
                log.info("{} is renaming the account with id {} to {}", account.getUsername(), id, username);
                accountService.addModEvent(new Event(EventType.NAME_CHANGE, id, username));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/confirm/{id}")
    public void promptConfirm(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            String text = StringEscapeUtils.escapeJava(HtmlUtils.htmlEscape(wsMessage.getContent()));

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR) || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
                return;
            } else {
                log.info("{} is prompting an confirm to the account with id {} with '{}'", account.getUsername(), id, text);
                accountService.addModEvent(new Event(EventType.CONFIRM, id, text));
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/mod/mod/{id}")
    public void mod(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("id") Long id) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || !(account.getAccessRole().equals(AccountAccessRole.OWNER))) {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
                return;
            }
            log.info("{} is modding the account with id {}", account.getUsername(), id);
            accountService.addModEvent(new Event(EventType.MOD, id));
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
