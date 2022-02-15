package de.kaliburg.morefair.ladder;

import de.kaliburg.morefair.FairController;
import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.moderation.controller.ModerationController;
import de.kaliburg.morefair.moderation.data.ModServerMessageData;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Log4j2
public class RankerController {
    public static final String LADDER_DESTINATION = "/queue/ladder/";
    public static final String LADDER_PRIVATE_UPDATE_DESTINATION = "/queue/ladder/updates/";
    public static final String LADDER_UPDATE_DESTINATION = "/topic/ladder/";
    public static final String GLOBAL_UPDATE_DESTINATION = "/topic/global/";
    private final RankerService rankerService;
    private final AccountService accountService;
    private final WSUtils wsUtils;

    public RankerController(RankerService rankerService, AccountService accountService, WSUtils wsUtils) {
        this.rankerService = rankerService;
        this.accountService = accountService;
        this.wsUtils = wsUtils;
    }


    @MessageMapping("/ladder/init/{number}")
    public void initLadder(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("number") Integer number) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/init/{} from {}", number, uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.FORBIDDEN);
                return;
            }

            Ranker ranker = rankerService.findHighestActiveRankerByAccount(account);

            if (ranker == null) {
                rankerService.getLadderSem().acquire();
                try {
                    ranker = rankerService.createNewActiveRankerForAccountOnLadder(account, 1);
                } finally {
                    rankerService.getLadderSem().release();
                }
            }

            if (account.getAccessRole().equals(AccountAccessRole.OWNER) || account.getAccessRole().equals(AccountAccessRole.MODERATOR)
                    || number == FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole()
                    || number <= ranker.getLadder().getNumber()) {
                LadderViewDTO l = rankerService.findAllRankerByLadderAreaAndAccount(number, account);
                wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, l);
            } else {
                wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, "Can't get permission to view the ladder.", HttpStatus.FORBIDDEN);
            }

        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/bias")
    public void buyBias(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] BIAS: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.BIAS, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/multi")
    public void buyMulti(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] MULTI: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.MULTI, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/vinegar")
    public void throwVinegar(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] VINEGAR: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.VINEGAR, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/promote")
    public void promote(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] PROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.PROMOTE, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/asshole")
    public void beAsshole(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/asshole from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] ASSHOLE: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.ASSHOLE, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/auto-promote")
    public void buyAutoPromote(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null || account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                return;
            }
            Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder().getNumber();
            log.debug("[L{}] AUTOPROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(), wsMessage.getEvent());
            ModServerMessageData data = new ModServerMessageData(account.getId(), sha.getDestination(), wsMessage.getContent(), wsMessage.getEvent());
            wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
            rankerService.addEvent(num, new Event(EventType.AUTO_PROMOTE, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
