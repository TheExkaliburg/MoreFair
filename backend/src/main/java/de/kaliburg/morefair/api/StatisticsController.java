package de.kaliburg.morefair.api;

import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.results.ActivityAnalysisEntity;
import de.kaliburg.morefair.statistics.results.RoundStatisticsEntity;
import de.kaliburg.morefair.statistics.services.RoundResultService;
import de.kaliburg.morefair.statistics.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

  private final RoundService roundService;
  private final StatisticsService statisticsService;
  private final RoundResultService roundResultService;

  @GetMapping(value = "/round/raw", produces = "application/json")
  public ResponseEntity<?> getRoundResults(
      @RequestParam(name = "number", required = false) Integer roundNumber) {
    try {
      if (roundNumber == null) {
        roundNumber = roundService.getCurrentRound().getNumber() - 1;
      }

      if (roundNumber >= roundService.getCurrentRound().getNumber()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      RoundResultsDto results = roundResultService.getRoundResults(roundNumber);
      if (results == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(results, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/round", produces = "application/json")
  public ResponseEntity<RoundStatisticsEntity> getRoundStatistics(
      @RequestParam(name = "round", required = false) Integer roundNumber) {
    try {
      if (roundNumber == null) {
        roundNumber = roundService.getCurrentRound().getNumber() - 1;
      }

      if (roundNumber >= roundService.getCurrentRound().getNumber()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      RoundStatisticsEntity results = statisticsService.getRoundStatistics(roundNumber);
      if (results == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(results, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/activity", produces = "application/json")
  public ResponseEntity<ActivityAnalysisEntity> getRoundStatistics() {
    try {
      ActivityAnalysisEntity results = statisticsService.getActivityAnalysis();
      if (results == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(results, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
