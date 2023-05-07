package de.kaliburg.morefair.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class FairController {

  public static final String TOPIC_TICK_DESTINATION = "/game/tick";


  @RequestMapping(value = "{path:[^.]*}")
  public String fallback() {
    return "forward:/";
  }
}
