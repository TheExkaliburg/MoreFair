package de.kaliburg.morefair.statistics.results;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "generalAnalysis")
@Getter
public class ActivityAnalysisEntity {

  private Date createdOn;
  private List<TimePerAccount> timePerAccount;
  private List<TimePerHour> timePerHour;
  private List<TimePerWeekday> timePerWeekday;
  private List<TimePerDay> timePerDay;

  @Getter
  public static class TimePerAccount {

    private Long account;
    private Double avgSeconds;
    private Long totalSeconds;
    private Long days;
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

    private Date date;
    private Long totalSeconds;
  }
}
