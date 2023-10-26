package de.kaliburg.morefair.statistics.records;

import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.RankerEntity;
import lombok.NonNull;

public class LadderRecord {

  @NonNull
  private Integer number;
  @NonNull
  private Integer scaling;
  @NonNull
  private Integer rankers;
  @NonNull
  private Integer activeRankers;

  public LadderRecord(LadderEntity ladder) {
    this.number = ladder.getNumber();
    this.scaling = ladder.getScaling();
    this.rankers = ladder.getRankers().size();
    this.activeRankers = (int) ladder.getRankers().stream().filter(RankerEntity::isGrowing).count();
  }
}
