package de.kaliburg.morefair.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class MoreController {
    @GetMapping("/fair")
    public String getIndex(){
        return "fair";
    }
}
