package de.kaliburg.morefair;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class IndexController
{
    private final UserService userService;

    public IndexController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/less")
    public String styled(Model model){
        model.addAttribute("users", userService.getUsers());

        return "less";
    }

    @PostMapping(path = "/user", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<User>> create(@RequestBody User newUser){
        System.out.println(newUser);
        userService.saveUser(newUser);

        return new ResponseEntity<>(userService.getUsers(), HttpStatus.CREATED);
    }

    @GetMapping(value = "/ladder", produces = "application/json")
    public ResponseEntity<List<User>> styled(){
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);


    }

}
