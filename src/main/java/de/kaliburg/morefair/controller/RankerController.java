package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@Log4j2
public class RankerController {
    private final RankerService rankerService;
    private final AccountService accountService;

    public RankerController(RankerService rankerService, AccountService accountService) {
        this.rankerService = rankerService;
        this.accountService = accountService;
    }

    @GetMapping(value = "/fair/ranker", produces = "application/json")
    public ResponseEntity<LadderViewDTO> getLadder(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("GET /fair/rankers from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(rankerService.findAllRankerByHighestLadderAreaAndAccount(account), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID from {} using 'POST /fair/login {}'", request.getRemoteAddr(), uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/fair/ranker/bias", produces = "application/json")
    public ResponseEntity<Void> buyBias(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/buy from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.buyBias(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/ranker/multiplier", produces = "application/json")
    public ResponseEntity<Void> buyMulti(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/multiplier from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.buyMulti(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/ranker/promote", produces = "application/json")
    public ResponseEntity<Void> promote(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/promote from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.promote(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
