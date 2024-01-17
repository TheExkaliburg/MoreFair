package de.kaliburg.morefair.statistics.records;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
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
