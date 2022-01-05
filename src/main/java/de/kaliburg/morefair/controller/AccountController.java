package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.AccountService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@Log4j2
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PutMapping(path = "/fair/account", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/json")
    public ResponseEntity<Void> changeUsername(String username, @CookieValue(name = "_uuid", defaultValue = "") String uuid) {
        log.debug("PUT /fair/account {} {}", uuid, username);
        username = username.substring(0, Math.min(32, username.length()));
        username = StringEscapeUtils.escapeJava(username);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                accountService.updateUsername(account, username);
                return new ResponseEntity<>(HttpStatus.OK);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/fair/loginc", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/json")
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
                    accountService.login(account);
                    return new ResponseEntity<>(account.dto(), HttpStatus.OK);
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

}
