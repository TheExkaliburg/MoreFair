package de.kaliburg.morefair.moderation.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class ModerationController {

    public ModerationController() {

    }

    @GetMapping("/moderation")
    public String getIndex() {
        return "moderation";
    }
}
