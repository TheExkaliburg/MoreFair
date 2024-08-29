package de.kaliburg.morefair.statistics.records.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "throwVinegar")
@Getter
@Setter
public class ThrowVinegarRecordEntity extends AbstractGameEventRecord {

  @NonNull
  private RankerRecord target;
  @NonNull
  private RankerRecord second;

  public ThrowVinegarRecordEntity(@NonNull RankerRecord ranker, @NonNull RankerRecord target,
      @NonNull RankerRecord second, @NonNull LadderRecord ladder, @NonNull RoundRecord round) {
    super(ranker, ladder, round);
    this.target = target;
    this.second = second;
  }
}
