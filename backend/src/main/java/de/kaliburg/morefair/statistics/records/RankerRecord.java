package de.kaliburg.morefair.statistics.records;

import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class RankerRecord {


  private long account;
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
  private boolean autoPromote;
  @NonNull
  private Integer round;

  public RankerRecord(RankerEntity ranker) {
    this.account = ranker.getAccountId();
    this.rank = ranker.getRank();
    this.bias = ranker.getBias();
    this.multi = ranker.getMultiplier();
    this.points = ranker.getPoints();
    this.power = ranker.getPower();
    this.grapes = ranker.getGrapes();
    this.vinegar = ranker.getVinegar();
    this.autoPromote = ranker.isAutoPromote();
    // FIXME: this.round = ranker.getLadderId().getRound().getNumber();
  }
}
