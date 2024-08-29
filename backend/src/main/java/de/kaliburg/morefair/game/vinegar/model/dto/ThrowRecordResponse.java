package de.kaliburg.morefair.game.vinegar.model.dto;

import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ThrowRecordResponse {

  private long accountId;
  private long targetId;
  private long timestamp;
  private int ladderNumber;
  private int roundNumber;
  private String vinegarThrown;
  private int percentage;
  private VinegarSuccessType successType;

}
