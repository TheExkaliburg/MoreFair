package de.kaliburg.morefair.statistics;

import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "multi")
public class MultiRecordEntity extends AbstractGameEventRecord {

  public MultiRecordEntity(@NonNull RankerRecord ranker, @NonNull LadderRecord ladder,
      @NonNull RoundRecord round) {
    super(ranker, ladder, round);
  }
}
