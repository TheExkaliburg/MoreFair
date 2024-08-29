package de.kaliburg.morefair.statistics.records.model;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.util.List;
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

  public LadderRecord(LadderEntity ladder, List<RankerEntity> rankers) {
    this.number = ladder.getNumber();
    this.scaling = ladder.getScaling();
    this.rankers = rankers.size();
    this.activeRankers = (int) rankers.stream().filter(RankerEntity::isGrowing).count();
  }
}
