package de.kaliburg.morefair.statistics.records;

import de.kaliburg.morefair.game.round.RoundEntity;
import lombok.NonNull;

public class RoundRecord {

  @NonNull
  private Integer number;
  @NonNull
  private LadderRecord firstLadder;

  public RoundRecord(RoundEntity round) {
    this.number = round.getNumber();
    this.firstLadder = round.getLadders().stream().filter(l -> l.getNumber() == 1)
        .map(LadderRecord::new).findFirst().orElseThrow();
  }

}
