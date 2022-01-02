package de.kaliburg.morefair.controller.chat;

import de.kaliburg.morefair.dto.chat.ChatDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.service.chat.ChatService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Log4j2
@Controller
public class ChatController {
    private final ChatService chatService;
    private final AccountService accountService;
    private final RankerService rankerService;

    public ChatController(ChatService chatService, AccountService accountService, RankerService rankerService) {
        this.chatService = chatService;
        this.accountService = accountService;
        this.rankerService = rankerService;
    }

    @GetMapping(value = "/fair/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatDTO> getChat(@RequestParam Integer ladder, @CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        log.debug("GET /fair/chat from {} for ladder number {}", uuid, ladder);
        uuid = StringEscapeUtils.escapeJava(uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            if (ladder <= rankerService.findHighestRankerByAccount(account).getLadder().getNumber()) {
                ChatDTO c = chatService.getChat(ladder);
                log.info("{}", c);
                return new ResponseEntity<>(c, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID {} using 'GET /fair/chat from {}", uuid, request.getRemoteAddr());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/chat", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> postChat(Integer ladder, String message, @CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        log.info("POST /fair/chat from {} with message {} for ladder number {}", uuid, message, ladder);
        uuid = StringEscapeUtils.escapeJava(uuid);
        message = StringEscapeUtils.escapeJava(message);
        try {
            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
                if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                if (ladder <= rankerService.findHighestRankerByAccount(account).getLadder().getNumber()) {
                    chatService.writeMessage(account, ladder, message);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID {} using 'GET /fair/chat from {}", uuid, request.getRemoteAddr());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
