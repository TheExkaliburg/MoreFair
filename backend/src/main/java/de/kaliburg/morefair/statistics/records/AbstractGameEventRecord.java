package de.kaliburg.morefair.statistics.records;

import java.time.Instant;
import lombok.Data;
import lombok.NonNull;

@Data
public abstract class AbstractGameEventRecord {

  @NonNull
  private Instant createdOn = Instant.now();
  @NonNull
  private RankerRecord ranker;
  @NonNull
  private LadderRecord ladder;
  @NonNull
  private RoundRecord round;
}
