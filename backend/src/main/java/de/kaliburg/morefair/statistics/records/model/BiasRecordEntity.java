package de.kaliburg.morefair.statistics.records.model;

import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bias")
public class BiasRecordEntity extends AbstractGameEventRecord {

  public BiasRecordEntity(@NonNull RankerRecord ranker, @NonNull LadderRecord ladder,
      @NonNull RoundRecord round) {
    super(ranker, ladder, round);
  }
}
