package de.kaliburg.morefair.controller;

import com.google.gson.Gson;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InvalidArgumentsException;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class AccountController
{
    private final AccountService accountService;
    private final LadderService ladderService;
    private final RankerService rankerService;

    public AccountController(AccountService accountService, LadderService ladderService,
            RankerService rankerService)
    {
        this.accountService = accountService;
        this.ladderService = ladderService;
        this.rankerService = rankerService;
    }

    @GetMapping("/")
    public String getIndex(){
        return "less";
    }

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> postLogin(@RequestBody UUID uuid){
        log.debug("POST /login" + uuid);
        Account account = accountService.findAccountByUUID(uuid);
        if(account == null) return new ResponseEntity<>(accountService.createNewAccount(), HttpStatus.CREATED);

        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @PostMapping(value = "/ladder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<Ranker>> postLadder(@RequestBody UUID uuid){
        log.debug("POST /ladder" + uuid);
        Account account = accountService.findAccountByUUID(uuid);
        if(account == null) account = accountService.createNewAccount();

        return new ResponseEntity<>(rankerService.findAllRankerForHighestLadderAreaForAccount(account), HttpStatus.OK);
    }

    @GetMapping(value = "/register", produces = "application/json")
    public ResponseEntity<Account> getRegister() {
        log.debug("GET /register");
        return new ResponseEntity<>(accountService.createNewAccount(), HttpStatus.CREATED);
    }

}
