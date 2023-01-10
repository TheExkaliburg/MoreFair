package de.kaliburg.morefair.statistics;

import java.time.Instant;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "multi")
@Data
public class MultiEntity {

  @NonNull
  private Instant createdOn = Instant.now();
  @NonNull
  private RankerRecord ranker;
  @NonNull
  private LadderRecord ladder;
  @NonNull
  private RoundRecord round;
}
