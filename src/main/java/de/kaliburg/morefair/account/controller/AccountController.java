package de.kaliburg.morefair.account.controller;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@Log4j2
public class AccountController {
    private static final String LOGIN_DESTINATION = "/queue/account/login";

    private final AccountService accountService;
    private final RankerService rankerService;
    private final WSUtils wsUtils;

    public AccountController(AccountService accountService, RankerService rankerService, WSUtils wsUtils) {
        this.accountService = accountService;
        this.rankerService = rankerService;
        this.wsUtils = wsUtils;
    }

    @PostMapping(path = "/fair/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/json")
    public ResponseEntity<AccountDetailsDTO> postLogin(String uuid, HttpServletRequest request) {
        log.debug("POST /fair/login {}", uuid);
        if (uuid.isBlank()) {
            return new ResponseEntity<>(accountService.createNewAccount(), HttpStatus.CREATED);
        }
        try {
            try {
                DatabaseWriteSemaphore.getInstance().acquire();
                Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
                if (account == null) {
                    return new ResponseEntity<>(accountService.createNewAccount(),
                            HttpStatus.CREATED);
                } else {
                    accountService.updateActivity(account);
                    return new ResponseEntity<>(account.convertToDTO(), HttpStatus.OK);
                }
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID from {} using 'POST /fair/login {}'",
                    request.getRemoteAddr(), uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @MessageMapping("/account/login")
    public void login(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/account/login {}", uuid);
            if (uuid.isBlank()) {
                if (wsUtils.canCreateUser(sha)) {
                    wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, accountService.createNewAccount(), HttpStatus.CREATED);
                } else {
                    wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
                }
                return;
            }

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) {
                if (wsUtils.canCreateUser(sha)) {
                    wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, accountService.createNewAccount(), HttpStatus.CREATED);
                } else {
                    wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
                }
                return;
            } else {
                accountService.updateActivity(account);
                wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, account.convertToDTO());
            }

        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/account/name")
    public void changeUsername(SimpMessageHeaderAccessor sha, WSMessage wsMessage) throws Exception {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            String username = wsMessage.getContent();
            username = username.trim();
            if (username.length() > 32) username = username.substring(0, 32);
            username = StringEscapeUtils.escapeJava(HtmlUtils.htmlEscape(username));

            log.debug("/app/account/name {} {}", uuid, username);

            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null) {
                Event event = new Event(EventType.NAME_CHANGE, account.getId());
                event.setData(username);
                rankerService.addGlobalEvent(event);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
