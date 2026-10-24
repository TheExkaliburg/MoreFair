package de.kaliburg.morefair.statistics.records.model;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.util.List;

public record LadderRecord(
    Integer number,
    Integer scaling,
    Integer rankers,
    Integer activeRankers) {

  public LadderRecord(LadderEntity ladder, List<RankerEntity> rankers) {
    this(ladder.getNumber(), ladder.getScaling(), rankers.size(),
        (int) rankers.stream().filter(RankerEntity::isGrowing).count());
  }
}
