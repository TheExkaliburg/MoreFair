package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InvalidArgumentsException;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
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
    public String styled(Model model){
        model.addAttribute("users", accountService.getUsers());
        return "less";
    }

    @PostMapping(path = "/user", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<Account>> create(@RequestBody Account newUser){
        accountService.saveUser(newUser);

        return new ResponseEntity<>(accountService.getUsers(), HttpStatus.CREATED);
    }

    @GetMapping(value = "/ladder", produces = "application/json")
    public ResponseEntity<List<Ranker>> styled(){
        try{
            return new ResponseEntity<>(rankerService.findAllRankerForHighestLadderAreaForAccount(new Account()), HttpStatus.OK);
        }catch(InvalidArgumentsException e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

}
