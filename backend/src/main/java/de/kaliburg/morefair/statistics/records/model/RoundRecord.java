package de.kaliburg.morefair.statistics.records.model;

import de.kaliburg.morefair.game.round.model.RoundEntity;

public record RoundRecord(
    Long id,
    Long seasonId,
    Integer number,
    LadderRecord firstLadder) {

  public RoundRecord(RoundEntity round, LadderRecord firstLadder) {
    this(round.getId(), round.getSeasonId(), round.getNumber(), firstLadder);
  }
}
