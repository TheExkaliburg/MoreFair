package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<LadderViewDTO> postLadder(@RequestParam String uuid, HttpServletRequest request) {
        log.debug("GET /fair/rankers?uuid={}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(rankerService.findAllRankerByHighestLadderAreaAndAccount(account), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID from {} using 'POST /fair/login {}'", request.getRemoteAddr(), uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
