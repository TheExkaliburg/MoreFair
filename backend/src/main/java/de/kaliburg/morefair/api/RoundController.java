package de.kaliburg.morefair.api;

import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.dto.RoundResultsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class RoundController {

  private final RoundService roundService;

  public RoundController(RoundService roundService) {
    this.roundService = roundService;
  }

  @GetMapping(value = "/roundStats", produces = "application/json")
  public ResponseEntity<RoundResultsDto> getStatistics(
      @RequestParam(name = "round", required = false) Integer roundNumber) {
    try {
      if (roundNumber == null) {
        roundNumber = roundService.getCurrentRound().getNumber() - 1;
      }

      if (roundNumber >= roundService.getCurrentRound().getNumber()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      RoundResultsDto results = roundService.getRoundResults(roundNumber);
      if (results == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(results, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
