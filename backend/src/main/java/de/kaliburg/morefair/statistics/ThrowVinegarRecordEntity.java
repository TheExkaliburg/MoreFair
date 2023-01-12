package de.kaliburg.morefair.statistics;

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

  public ThrowVinegarRecordEntity(@NonNull RankerRecord ranker, @NonNull RankerRecord target,
      @NonNull LadderRecord ladder, @NonNull RoundRecord round) {
    super(ranker, ladder, round);
    this.target = target;
  }
}
