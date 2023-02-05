package de.kaliburg.morefair.statistics.results;

import java.util.List;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roundStatistics")
@Getter
public class RoundStatisticsEntity {

  private Integer roundId;
  private List<Champion> champions;

  @Getter
  public static class Champion {

    private Integer accountId;
    private String username;
    private List<Integer> points;
    private Integer total;
  }
}
