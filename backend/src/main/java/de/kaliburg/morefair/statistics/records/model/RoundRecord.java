package de.kaliburg.morefair.statistics.records.model;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class RoundRecord {

  @NonNull
  private Integer number;
  @NonNull
  private LadderRecord firstLadder;

  public RoundRecord(RoundEntity round, Map<LadderEntity, List<RankerEntity>> ladders) {
    this.number = round.getNumber();
    this.firstLadder = ladders.entrySet().stream().filter(l -> l.getKey().getNumber() == 1)
        .map(l -> new LadderRecord(l.getKey(), l.getValue())).findFirst().orElseThrow();
  }

}
