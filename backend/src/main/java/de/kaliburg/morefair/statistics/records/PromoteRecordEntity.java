package de.kaliburg.morefair.statistics.records;

import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "promote")
public class PromoteRecordEntity extends AbstractGameEventRecord {

  public PromoteRecordEntity(@NonNull RankerRecord ranker, @NonNull LadderRecord ladder,
      @NonNull RoundRecord round) {
    super(ranker, ladder, round);
  }
}
