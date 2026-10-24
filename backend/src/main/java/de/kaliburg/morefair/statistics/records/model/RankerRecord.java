package de.kaliburg.morefair.statistics.records.model;

import de.kaliburg.morefair.game.ranker.model.RankerEntity;

public record RankerRecord(
    long account,
    Integer rank,
    Integer bias,
    Integer multi,
    String points,
    String power,
    String grapes,
    String vinegar,
    boolean autoPromote) {

  public RankerRecord(RankerEntity ranker) {
    this(ranker.getAccountId(), ranker.getRank(), ranker.getBias(), ranker.getMultiplier(),
        ranker.getPoints().toString(), ranker.getPower().toString(), ranker.getGrapes().toString(),
        ranker.getVinegar().toString(), ranker.isAutoPromote());
  }
}
