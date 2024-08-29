package de.kaliburg.morefair.statistics.services;

import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;

public interface RoundResultService {

  RoundResultsDto getRoundResults(Integer seasonNumber, Integer roundNumber);

}
