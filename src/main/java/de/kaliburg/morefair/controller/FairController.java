package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.InfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;

@Controller
@Slf4j
public class FairController {
    public static final Integer UPDATE_LADDER_STEPS_BEFORE_SYNC = 10;
    public static final Integer UPDATE_CHAT_STEPS_BEFORE_SYNC = 30;
    public final static Integer LADDER_AREA_SIZE = 10;
    public final static Integer PEOPLE_FOR_PROMOTE = 10;
    public final static BigInteger POINTS_FOR_PROMOTE = BigInteger.valueOf(250000000L);
    public final static Integer LADDER_AREA_SIZE_SERVER = 30;

    @GetMapping("/")
    public String getIndex() {
        return "fair";
    }

    @GetMapping(value = "/fair/info", produces = "application/json")
    public ResponseEntity<InfoDTO> getInfo() {
        return new ResponseEntity<>(new InfoDTO(), HttpStatus.OK);
    }
}
