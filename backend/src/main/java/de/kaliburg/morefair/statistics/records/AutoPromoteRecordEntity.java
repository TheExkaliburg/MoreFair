package de.kaliburg.morefair.statistics.records;

import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "autoPromote")
public class AutoPromoteRecordEntity extends AbstractGameEventRecord {

  public AutoPromoteRecordEntity(@NonNull RankerRecord ranker, @NonNull LadderRecord ladder,
      @NonNull RoundRecord round) {
    super(ranker, ladder, round);
  }
}
