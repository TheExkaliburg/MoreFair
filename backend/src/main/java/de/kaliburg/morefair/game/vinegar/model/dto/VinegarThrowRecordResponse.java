package de.kaliburg.morefair.game.vinegar.model.dto;

import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VinegarThrowRecordResponse {

  public List<ThrowRecord> throwRecords;

  @Data
  @Builder
  @AllArgsConstructor
  public static class ThrowRecord {

    public long accountId;
    public long targetId;
    public long timestamp;
    public int ladderNumber;
    public int roundNumber;
    public String vinegarThrown;
    public int percentage;
    public VinegarSuccessType successType;

  }

}
