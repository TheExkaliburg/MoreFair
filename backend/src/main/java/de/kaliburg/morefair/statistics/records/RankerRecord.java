package de.kaliburg.morefair.statistics.records;

import de.kaliburg.morefair.game.round.RankerEntity;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class RankerRecord {

  @NonNull
  private Long account;
  @NonNull
  private Integer rank;
  @NonNull
  private Integer bias;
  @NonNull
  private Integer multi;
  @NonNull
  private BigInteger points;
  @NonNull
  private BigInteger power;
  @NonNull
  private BigInteger grapes;
  @NonNull
  private BigInteger vinegar;
  @NonNull
  private boolean autoPromote;
  @NonNull
  private Integer round;

  public RankerRecord(RankerEntity ranker) {
    this.account = ranker.getAccount().getId();
    this.rank = ranker.getRank();
    this.bias = ranker.getBias();
    this.multi = ranker.getMultiplier();
    this.points = ranker.getPoints();
    this.power = ranker.getPower();
    this.grapes = ranker.getGrapes();
    this.vinegar = ranker.getVinegar();
    this.autoPromote = ranker.isAutoPromote();
    this.round = ranker.getLadder().getRound().getNumber();
  }
}
