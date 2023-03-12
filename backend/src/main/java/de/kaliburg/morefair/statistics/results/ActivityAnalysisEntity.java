package de.kaliburg.morefair.statistics.results;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "generalAnalysis")
@Getter
public class ActivityAnalysisEntity {

  private OffsetDateTime createdOn;
  private List<TimePerAccount> timePerAccount;
  private List<TimePerHour> timePerHour;
  private List<TimePerWeekday> timePerWeekday;
  private List<TimePerDay> timePerDay;

  @Getter
  public static class TimePerAccount {

    private Long account;
    private Double avgSeconds;
    private Long totalSeconds;
  }

  @Getter
  public static class TimePerHour {

    private Integer hour;
    private Double avgSeconds;
    private Long totalSeconds;
  }


  @Getter
  public static class TimePerWeekday {

    private Integer weekday;
    private Double avgSeconds;
    private Long totalSeconds;
  }

  @Getter
  public static class TimePerDay {

    private OffsetDateTime date;
    private Long totalSeconds;
  }
}
