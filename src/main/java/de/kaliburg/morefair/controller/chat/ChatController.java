package de.kaliburg.morefair.controller.chat;

import de.kaliburg.morefair.dto.chat.ChatDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.service.chat.ChatService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Log4j2
@Controller
public class ChatController {
    private static final String CHAT_DESTINATION = "/queue/chat/";
    private final ChatService chatService;
    private final AccountService accountService;
    private final RankerService rankerService;
    private final WSUtils wsUtils;

    public ChatController(ChatService chatService, AccountService accountService, RankerService rankerService, WSUtils wsUtils) {
        this.chatService = chatService;
        this.accountService = accountService;
        this.rankerService = rankerService;
        this.wsUtils = wsUtils;
    }

    @MessageMapping("/initChat/{number}")
    public void initChat(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("number") Integer number) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.info("/app/initChat/{} from {}", number, uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION + number, HttpStatus.FORBIDDEN);
            if (number <= rankerService.findHighestRankerByAccount(account).getLadder().getNumber()) {
                ChatDTO c = chatService.getChat(number);
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION + number, c);
            } else {
                wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION + number, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION + number, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION + number, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/postChat/{number}")
    public void postChat(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("number") Integer number) throws Exception {
        String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
        log.info("/app/postChat/{} from {}", number, uuid);
    }

    @SubscribeMapping("/topic/chat/{number}")
    public void subscribeToChat(StompHeaderAccessor stompHeaderAccessor, @DestinationVariable("number") Integer number) {
        log.info("Subscribe to /app/initChat/{} from {}", number, number);
    }
}
